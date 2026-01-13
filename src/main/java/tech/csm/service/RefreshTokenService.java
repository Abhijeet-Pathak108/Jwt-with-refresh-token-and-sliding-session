package tech.csm.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.csm.entity.RefreshToken;
import tech.csm.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

//    private static final long REFRESH_TOKEN_DAYS = 7;
	 private static final int refreshTokenMinute = 1;
	 
	 public long getRefreshTokenMinute() {
		    return refreshTokenMinute;
		}

    @Autowired
    private RefreshTokenRepository repo;

    public RefreshToken create(String username) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        token.setExpiry(Instant.now().plus(refreshTokenMinute,ChronoUnit.MINUTES));
//        token.setExpiry(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS));
//        token.setExpiry(Instant.now().plus(1,ChronoUnit.MINUTES));
        return repo.save(token);
    }

    public RefreshToken verify(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiry().isBefore(Instant.now())) {
            repo.delete(rt);
            throw new RuntimeException("Refresh token expired");
        }
        return rt;
    }

    // Sliding session
    public void extend(RefreshToken token) {
//        token.setExpiry(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS));
    	token.setExpiry(Instant.now().plus(refreshTokenMinute,ChronoUnit.MINUTES));
        repo.save(token);
    }

//    public void delete(String token) {
//        repo.deleteByToken(token);
//    }
    public void delete(String token) {
        try {
            Optional<RefreshToken> refreshToken = repo.findByToken(token);
            if (refreshToken.isPresent()) {
                repo.delete(refreshToken.get());
                System.out.println("RefreshToken deleted from repository");
            } else {
                System.out.println("RefreshToken not found in repository");
            }
        } catch (Exception e) {
            System.out.println("Error in delete method: " + e.getMessage());
            throw e;
        }
    }
}

