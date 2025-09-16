package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${spring.mail.from:starshop.a.6868@gmail.com}")
    private String fromEmail;
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Send OTP email with HTML template
     */
    public void sendOtpEmail(String toEmail, String otp, String firstName) {
        try {
            logger.info("=== SENDING OTP EMAIL ===");
            logger.info("From: {} (StarShop 🌸)", fromEmail);
            logger.info("To: {}", toEmail);
            logger.info("Subject: 🌸 Mã xác thực StarShop - {}", otp);
            logger.info("OTP Code: {}", otp);
            logger.info("User: {}", firstName != null ? firstName : "N/A");
            
            String htmlContent = buildOtpEmailTemplate(otp, firstName);
            logger.info("Email content generated successfully (HTML length: {} chars)", htmlContent.length());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "StarShop 🌸");
            helper.setTo(toEmail);
            helper.setSubject("🌸 Mã xác thực StarShop - " + otp);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            logger.info("✅ OTP email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send OTP email to: {} - Error: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    
    /**
     * Send welcome email after successful registration
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            logger.info("=== SENDING WELCOME EMAIL ===");
            logger.info("From: {} (StarShop 🌸)", fromEmail);
            logger.info("To: {}", toEmail);
            logger.info("Subject: 🌸 Chào mừng bạn đến với StarShop!");
            logger.info("User: {}", firstName != null ? firstName : "N/A");
            
            String htmlContent = buildWelcomeEmailTemplate(firstName);
            logger.info("Welcome email content generated (HTML length: {} chars)", htmlContent.length());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "StarShop 🌸");
            helper.setTo(toEmail);
            helper.setSubject("🌸 Chào mừng bạn đến với StarShop!");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            logger.info("✅ Welcome email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send welcome email to: {} - Error: {}", toEmail, e.getMessage());
            // Don't throw exception for welcome email as it's not critical
        }
    }
    
    /**
     * Send password reset confirmation email
     */
    public void sendPasswordResetConfirmationEmail(String toEmail, String firstName) {
        try {
            logger.info("=== SENDING PASSWORD RESET CONFIRMATION EMAIL ===");
            logger.info("From: {} (StarShop 🌸)", fromEmail);
            logger.info("To: {}", toEmail);
            logger.info("Subject: 🔒 Mật khẩu đã được đặt lại thành công");
            logger.info("User: {}", firstName != null ? firstName : "N/A");
            
            String htmlContent = buildPasswordResetConfirmationTemplate(firstName);
            logger.info("Password reset confirmation email content generated (HTML length: {} chars)", htmlContent.length());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "StarShop 🌸");
            helper.setTo(toEmail);
            helper.setSubject("🔒 Mật khẩu đã được đặt lại thành công");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            logger.info("✅ Password reset confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send password reset confirmation email to: {} - Error: {}", toEmail, e.getMessage());
            // Don't throw exception as password reset was successful
        }
    }
    
    private String buildOtpEmailTemplate(String otp, String firstName) {
        String currentDateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>StarShop OTP</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #fdf8f5;
                    }
                    .container {
                        background: linear-gradient(135deg, #EC407A 0%%, #F48FB1 100%%);
                        border-radius: 20px;
                        padding: 30px;
                        box-shadow: 0 10px 30px rgba(236, 64, 122, 0.2);
                        color: white;
                        text-align: center;
                    }
                    .header {
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 2rem;
                        font-weight: 700;
                        margin-bottom: 10px;
                    }
                    .tagline {
                        font-size: 1.1rem;
                        opacity: 0.9;
                        margin-bottom: 30px;
                    }
                    .otp-section {
                        background: rgba(255, 255, 255, 0.15);
                        border-radius: 15px;
                        padding: 25px;
                        margin: 20px 0;
                        backdrop-filter: blur(10px);
                    }
                    .otp-code {
                        font-size: 2.5rem;
                        font-weight: 700;
                        letter-spacing: 8px;
                        color: #fff;
                        margin: 15px 0;
                        text-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    .otp-label {
                        font-size: 1rem;
                        margin-bottom: 10px;
                        opacity: 0.9;
                    }
                    .expiry-info {
                        font-size: 0.9rem;
                        opacity: 0.8;
                        margin-top: 15px;
                    }
                    .security-note {
                        background: rgba(255, 255, 255, 0.1);
                        border-radius: 10px;
                        padding: 15px;
                        margin-top: 20px;
                        font-size: 0.9rem;
                        opacity: 0.85;
                    }
                    .footer {
                        margin-top: 30px;
                        font-size: 0.8rem;
                        opacity: 0.7;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            🌸 StarShop 🌸
                        </div>
                        <div class="tagline">Nơi kết nối yêu thương qua từng đóa hoa</div>
                    </div>
                    
                    <h2>Xin chào %s! 👋</h2>
                    <p>Bạn đã yêu cầu mã xác thực để đặt lại mật khẩu. Đây là mã OTP của bạn:</p>
                    
                    <div class="otp-section">
                        <div class="otp-label">Mã xác thực của bạn</div>
                        <div class="otp-code">%s</div>
                        <div class="expiry-info">⏰ Mã có hiệu lực trong 10 phút</div>
                    </div>
                    
                    <div class="security-note">
                        🔒 <strong>Lưu ý bảo mật:</strong><br>
                        • Không chia sẻ mã này với bất kỳ ai<br>
                        • StarShop sẽ không bao giờ yêu cầu mã OTP qua điện thoại<br>
                        • Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email
                    </div>
                    
                    <div class="footer">
                        <p>Email được gửi lúc: %s</p>
                        <p>© 2024 StarShop. Tất cả quyền được bảo lưu.</p>
                        <p>💌 Email này được gửi từ hệ thống tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName != null ? firstName : "bạn", otp, currentDateTime);
    }
    
    private String buildWelcomeEmailTemplate(String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chào mừng đến với StarShop</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #fdf8f5;
                    }
                    .container {
                        background: linear-gradient(135deg, #EC407A 0%%, #F48FB1 100%%);
                        border-radius: 20px;
                        padding: 30px;
                        box-shadow: 0 10px 30px rgba(236, 64, 122, 0.2);
                        color: white;
                        text-align: center;
                    }
                    .logo {
                        font-size: 2rem;
                        font-weight: 700;
                        margin-bottom: 20px;
                    }
                    .welcome-section {
                        background: rgba(255, 255, 255, 0.15);
                        border-radius: 15px;
                        padding: 25px;
                        margin: 20px 0;
                        backdrop-filter: blur(10px);
                    }
                    .benefits {
                        text-align: left;
                        margin: 20px 0;
                    }
                    .benefits li {
                        margin: 10px 0;
                        padding-left: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="logo">
                        🌸 StarShop 🌸
                    </div>
                    
                    <div class="welcome-section">
                        <h2>Chào mừng %s đến với StarShop! 🎉</h2>
                        <p>Cảm ơn bạn đã tham gia cộng đồng yêu hoa của chúng tôi!</p>
                        
                        <div class="benefits">
                            <h3>🌟 Quyền lợi thành viên:</h3>
                            <ul>
                                <li>🎁 Ưu đãi sinh nhật đặc biệt</li>
                                <li>📦 Theo dõi đơn hàng realtime</li>
                                <li>💾 Lưu địa chỉ giao hàng</li>
                                <li>⭐ Tích điểm thành viên</li>
                                <li>🚚 Miễn phí giao hàng từ 500k</li>
                            </ul>
                        </div>
                        
                        <p><strong>Hãy khám phá những bó hoa tuyệt đẹp ngay hôm nay!</strong></p>
                    </div>
                    
                    <div style="margin-top: 30px; font-size: 0.9rem; opacity: 0.8;">
                        <p>© 2024 StarShop - Nơi kết nối yêu thương qua từng đóa hoa</p>
                        <p>💌 Hơn 50,000 khách hàng tin dùng</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName != null ? firstName : "bạn");
    }
    
    private String buildPasswordResetConfirmationTemplate(String firstName) {
        String currentDateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mật khẩu đã được đặt lại</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #fdf8f5;
                    }
                    .container {
                        background: linear-gradient(135deg, #4CAF50 0%%, #81C784 100%%);
                        border-radius: 20px;
                        padding: 30px;
                        box-shadow: 0 10px 30px rgba(76, 175, 80, 0.2);
                        color: white;
                        text-align: center;
                    }
                    .success-icon {
                        font-size: 4rem;
                        margin-bottom: 20px;
                    }
                    .message-section {
                        background: rgba(255, 255, 255, 0.15);
                        border-radius: 15px;
                        padding: 25px;
                        margin: 20px 0;
                        backdrop-filter: blur(10px);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <h2>Mật khẩu đã được đặt lại thành công!</h2>
                    
                    <div class="message-section">
                        <p>Xin chào %s,</p>
                        <p>Mật khẩu tài khoản StarShop của bạn đã được đặt lại thành công vào lúc <strong>%s</strong>.</p>
                        <p>Bây giờ bạn có thể đăng nhập bằng mật khẩu mới.</p>
                        
                        <div style="margin-top: 20px; padding: 15px; background: rgba(255,255,255,0.1); border-radius: 10px;">
                            🔒 <strong>Lưu ý bảo mật:</strong><br>
                            Nếu bạn không thực hiện thao tác này, vui lòng liên hệ ngay với chúng tôi.
                        </div>
                    </div>
                    
                    <div style="margin-top: 30px; font-size: 0.9rem; opacity: 0.8;">
                        <p>© 2024 StarShop 🌸</p>
                        <p>Đội ngũ bảo mật StarShop</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName != null ? firstName : "bạn", currentDateTime);
    }
}
