package tech.csm.controller;

import org.springframework.http.HttpHeaders;


import java.time.Duration;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import tech.csm.dto.AuthRequest;
import tech.csm.dto.AuthResponse;
import tech.csm.entity.RefreshToken;
import tech.csm.service.RefreshTokenService;
import tech.csm.util.JwtUtil;

@RestController
//@CrossOrigin("*")
//@CrossOrigin(
//	    origins = "http://127.0.0.1:5500",
//	    allowCredentials = "true"
//	)

@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @GetMapping("/home")
    public String showHome() {
    	return "Welcome to JWT Authentication";
    }
    
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody AuthRequest request,
//                                   HttpServletResponse response) {
//
//        authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(
//                request.getUsername(),
//                request.getPassword()
//            )
//        );
//
//        String accessToken = jwtUtil.generateToken(request.getUsername());
//
//        RefreshToken refreshToken = refreshTokenService.create(request.getUsername());
//
//        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
//                .httpOnly(true)
//                .secure(false)          // 🔥 MUST be false for HTTP
//                .sameSite("None")
//                .path("/")
//                .maxAge(Duration.ofMinutes(1))
//                .build();
//
//        response.addHeader("Set-Cookie", cookie.toString());
//
//        return ResponseEntity.ok(
//            new AuthResponse(accessToken,refreshToken.getToken())
//        );
//    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request,
                                   HttpServletResponse response) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        String accessToken = jwtUtil.generateToken(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.create(request.getUsername());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        System.out.println(">>> Refresh cookie added: " + cookie);

        return ResponseEntity.ok(Map.of("accessToken", accessToken,"refreshToken",refreshToken.getToken()));
    }


    
//    @PostMapping("/refresh")
//    public ResponseEntity<?> refresh(
//            @RequestBody RefreshToken refreshToken) {
//
//        if (refreshToken.getToken() == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        RefreshToken token = refreshTokenService.verify(refreshToken.getToken());
//
//        String newAccessToken =
//                jwtUtil.generateToken(token.getUsername());
//
//        // Sliding expiration
//        refreshTokenService.extend(token);
//
//        return ResponseEntity.ok(
//            Map.of("accessToken", newAccessToken,"refreshToken",token.getToken())
//        );
//    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken token = refreshTokenService.verify(refreshToken);

        // issue new access token
        String newAccessToken = jwtUtil.generateToken(token.getUsername());

        // sliding session logic
        refreshTokenService.extend(token);
        
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenService.getRefreshTokenMinute() * 60)  // Convert minutes to seconds
                .sameSite("Lax")
                .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
            Map.of("accessToken", newAccessToken)
        );
    }
    
    
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(
//            @CookieValue(name = "refreshToken", required = false) String refreshToken,
//            HttpServletResponse response) {
//
//        if (refreshToken != null) {
//            refreshTokenService.delete(refreshToken);
//        }
//
//        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
//        		.httpOnly(true)
//        		.secure(false)
//                .path("/")
//                .sameSite("Lax")
//                .maxAge(0)
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
//        
//        System.out.println("Set-Cookie header: " + deleteCookie.toString());
//
//        return ResponseEntity.ok().build();
//    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        System.out.println("=== LOGOUT START ===");
        System.out.println("Token received: " + refreshToken);

        if (refreshToken != null) {
            try {
                System.out.println("Attempting to delete token from DB...");
                refreshTokenService.delete(refreshToken);
                System.out.println("Token deleted successfully");
            } catch (Exception e) {
                System.out.println("ERROR deleting token: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Creating delete cookie...");
        
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        
        System.out.println("Set-Cookie header added: " + deleteCookie.toString());
        System.out.println("=== LOGOUT END ===");

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
    


}

