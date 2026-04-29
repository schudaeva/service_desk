package com.example.vkr.service;

import com.example.vkr.dto.EmailMessageDto;
import com.example.vkr.entity.Request;
import com.example.vkr.entity.RequestStatus;
import com.example.vkr.entity.User;
import com.example.vkr.repository.RequestRepository;
import com.example.vkr.repository.RequestStatusRepository;
import com.example.vkr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailReceiveService {

    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private int imapPort;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.protocol}")
    private String protocol;

    @Value("${mail.imap.folder}")
    private String folderName;

    /**
     * Периодическая проверка новых писем (каждые 60 секунд)
     */
    @Scheduled(fixedDelayString = "${mail.imap.poll.interval:60000}")
    public void checkNewEmails() {
        log.info("Проверка новых писем...");

        try {
            // Подключаемся к почтовому ящику
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", protocol);
            props.setProperty("mail.imap.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore(protocol);
            store.connect(imapHost, imapPort, username, password);

            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);

            // Ищем только непрочитанные письма
            FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = folder.search(unseenFlagTerm);

            log.info("Найдено новых писем: {}", messages.length);

            for (Message message : messages) {
                try {
                    processEmail(message);
                    // Помечаем письмо как прочитанное
                    message.setFlag(Flags.Flag.SEEN, true);
                } catch (Exception e) {
                    log.error("Ошибка при обработке письма: {}", e.getMessage());
                }
            }

            folder.close(false);
            store.close();

        } catch (Exception e) {
            log.error("Ошибка при подключении к почтовому серверу: {}", e.getMessage());
        }
    }

    /**
     * Обработка одного письма
     */
    private void processEmail(Message message) throws Exception {
        EmailMessageDto emailDto = parseEmail(message);

        log.info("Обработка письма от: {}, тема: {}", emailDto.getFrom(), emailDto.getSubject());

        // Проверяем, не обрабатывали ли уже это письмо
        Optional<Request> existingRequest = requestRepository.findAll().stream()
                .filter(r -> emailDto.getMessageId().equals(r.getSourceId()))
                .findFirst();

        if (existingRequest.isPresent()) {
            log.info("Письмо с ID {} уже обработано", emailDto.getMessageId());
            return;
        }

        // Создаём заявку из письма
        Request request = createRequestFromEmail(emailDto);

//        // Отправляем подтверждение отправителю
//        sendConfirmationEmail(emailDto.getFrom(), request.getRequestId());

        log.info("Заявка №{} создана из письма", request.getRequestId());
    }

    /**
     * Парсинг письма
     */
    private EmailMessageDto parseEmail(Message message) throws Exception {
        EmailMessageDto dto = new EmailMessageDto();

        // Распарсим From - извлечём только email
        String from = message.getFrom()[0].toString();
        String cleanEmail = extractEmailFromFromString(from);
        dto.setFrom(cleanEmail);

        dto.setSubject(message.getSubject() != null ? message.getSubject() : "Без темы");
        dto.setReceivedDate(LocalDateTime.now());
        dto.setMessageId(message.getHeader("Message-ID") != null
                ? message.getHeader("Message-ID")[0]
                : String.valueOf(System.currentTimeMillis()));

        // Парсим содержимое
        StringBuilder content = new StringBuilder();
        extractContent(message, content, true);
        dto.setContent(content.toString());

        return dto;
    }
    /**
     * Извлекает email из строки вида "Имя Фамилия" <email@domain.com>
     */
    private String extractEmailFromFromString(String from) {
        if (from == null) return null;

        // Ищем email в угловых скобках: <...>
        int start = from.indexOf('<');
        int end = from.indexOf('>');

        if (start != -1 && end != -1 && start < end) {
            return from.substring(start + 1, end).trim();
        }

        // Если нет угловых скобок, проверяем, не является ли сама строка email'ом
        if (from.contains("@")) {
            return from.trim();
        }

        // Если ничего не нашли, возвращаем как есть
        return from;
    }
    /**
     * Рекурсивное извлечение текста из письма
     */
    private void extractContent(Part part, StringBuilder content, boolean isFirstTextPart) throws Exception {
        if (part.isMimeType("text/plain")) {
            // Обычный текст
            String text = part.getContent().toString();
            // Убираем лишние переносы строк
            text = text.trim();
            if (content.length() == 0 && text.length() > 0) {
                content.append(text);
            }
        } else if (part.isMimeType("text/html") && content.length() == 0) {
            // HTML используем только если нет plain text
            String html = part.getContent().toString();
            String text = html.replaceAll("<[^>]*>", " ").replaceAll("&nbsp;", " ").replaceAll("\\s+", " ").trim();
            if (content.length() == 0 && text.length() > 0) {
                content.append(text);
            }
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                extractContent(multipart.getBodyPart(i), content, false);
            }
        }
    }

    /**
     * Создание заявки из письма
     */
    private Request createRequestFromEmail(EmailMessageDto emailDto) {
        // Находим или создаём пользователя-отправителя
        User requester = findOrCreateUserFromEmail(emailDto.getFrom());

        RequestStatus newStatus = requestStatusRepository.findByCode("NEW")
                .orElseThrow(() -> new RuntimeException("Статус NEW не найден"));

        Request request = new Request();
        request.setTitle(emailDto.getSubject().length() > 255
                ? emailDto.getSubject().substring(0, 255)
                : emailDto.getSubject());

        String content = emailDto.getContent();
        if (content.length() > 4000) {
            content = content.substring(0, 4000) + "...\n[Сообщение обрезано]";
        }
        request.setDescription(content);
        request.setStatus(newStatus);
        request.setCreatedBy(requester);
        request.setSource("EMAIL");
        request.setSourceId(emailDto.getMessageId());
        request.setPriority("MEDIUM");
        request.setCreatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    /**
     * Поиск или создание пользователя по email
     */
    private User findOrCreateUserFromEmail(String email) {
        // email уже чистый, без кавычек и угловых скобок
        String username = email;
        if (email.contains("@")) {
            username = email.substring(0, email.indexOf("@"));
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Создаём нового пользователя
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFullName(username);
        // Генерируем случайный пароль
        newUser.setPassword("{noop}" + java.util.UUID.randomUUID().toString());
        newUser.setCreatedAt(LocalDateTime.now());

        // Назначаем роль REQUESTER (нужно добавить логику назначения роли)
        // roleRepository.findByName("REQUESTER").ifPresent(role -> newUser.getRoles().add(role));

        return userRepository.save(newUser);
    }

    /**
     * Отправка подтверждения отправителю
     */
    @Async
    public void sendConfirmationEmail(String to, Integer requestId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Service Desk] Ваша заявка №" + requestId + " создана");

            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #4CAF50;">Заявка №%d успешно создана</h2>
                    <p>Мы получили ваше обращение и зарегистрировали его под номером <strong>%d</strong>.</p>
                    <p>Вы можете отслеживать статус заявки в личном кабинете или по этой ссылке:</p>
                    <p><a href="http://localhost:8080/requests/%d">http://localhost:8080/requests/%d</a></p>
                    <hr>
                    <p style="color: #666; font-size: 12px;">Это автоматическое сообщение, пожалуйста, не отвечайте на него.</p>
                </body>
                </html>
                """, requestId, requestId, requestId, requestId);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Подтверждение отправлено на {}", to);

        } catch (Exception e) {
            log.error("Ошибка при отправке подтверждения: {}", e.getMessage());
        }
    }
}