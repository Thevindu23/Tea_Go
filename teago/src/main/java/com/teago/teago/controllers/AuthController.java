package com.teago.teago.controllers;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.User;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.UserRepository;
import com.teago.teago.Services.UserService;
import com.teago.teago.dto.LoginRequest;
import com.teago.teago.dto.RegisterRequest;
import com.teago.teago.dto.UserDTO;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final UserService            userService;
    private final UserRepository         userRepository;
    private final FactoryOwnerRepository factoryOwnerRepository;
    private final LandOwnerRepository    landOwnerRepository;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          FactoryOwnerRepository factoryOwnerRepository,
                          LandOwnerRepository landOwnerRepository) {
        this.userService            = userService;
        this.userRepository         = userRepository;
        this.factoryOwnerRepository = factoryOwnerRepository;
        this.landOwnerRepository    = landOwnerRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(org.springframework.security.web.csrf.CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return ResponseEntity.ok(Map.of("message", "registered"));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@jakarta.validation.Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest servletRequest, jakarta.servlet.http.HttpServletResponse servletResponse) {
        try {
            UserDTO dto  = userService.login(request);
            HttpSession session = servletRequest.getSession();
            servletRequest.changeSessionId();
            User    user = userRepository.findByUsername(request.getUsername()).orElse(null);

            String  roleOut         = "";
            Integer userId          = null;

            // Factory owner fields
            String  factoryName     = null;
            String  factoryLocation = null;
            Integer factoryOwnerId  = null;

            // Land owner fields
            Integer landOwnerId     = null;
            String  landSize        = null;

            if (user != null) {
                userId = user.getUserId();

                if (user.getRole() != null) {
                    switch (user.getRole()) {

                        case LandOwner:
                            roleOut = "LAND_OWNER";
                            LandOwner lo = landOwnerRepository
                                    .findByUser_UserId(userId).orElse(null);
                            if (lo != null) {
                                landOwnerId = lo.getLandOwnerId();
                                landSize    = lo.getLandSize() != null
                                        ? lo.getLandSize().toPlainString() : null;
                            }
                            break;

                        case FactoryOwner:
                            roleOut = "FACTORY_OWNER";
                            FactoryOwner fo = factoryOwnerRepository
                                    .findByUser_UserId(userId).orElse(null);
                            if (fo != null) {
                                factoryOwnerId  = fo.getFactoryOwnerId();
                                factoryName     = fo.getFactoryName();
                                factoryLocation = fo.getFactoryLocation();
                            }
                            break;

                        case Supervisor:
                            roleOut = "SUPERVISOR";
                            break;
                    }
                }
            }

            var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    dto.getUsername(), null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + roleOut)));
            var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository().saveContext(context, servletRequest, servletResponse);
            // Rotate the CSRF token after authentication.
            new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository().saveToken(null, servletRequest, servletResponse);
            // Persist login state in HTTP session so refresh/new page loads keep auth context.
            session.setAttribute("teago_userId", userId);
            session.setAttribute("teago_role", roleOut);
            session.setAttribute("teago_fullName", dto.getName());
            session.setAttribute("teago_username", dto.getUsername());
            session.setAttribute("teago_landOwnerId", landOwnerId);
            session.setAttribute("teago_landSize", landSize);
            session.setAttribute("teago_factoryOwnerId", factoryOwnerId);
            session.setAttribute("teago_factoryName", factoryName);
            session.setAttribute("teago_factoryLocation", factoryLocation);

            Map<String, Object> resp = new HashMap<>();
            resp.put("token",           "");
            resp.put("role",            roleOut);
            resp.put("userId",          userId);
            resp.put("fullName",        dto.getName());
            resp.put("username",        dto.getUsername());

            // Land owner specific fields (null for other roles)
            resp.put("landOwnerId",     landOwnerId);
            resp.put("landSize",        landSize);

            // Factory owner specific fields (null for other roles)
            resp.put("factoryOwnerId",  factoryOwnerId);
            resp.put("factoryName",     factoryName);
            resp.put("factoryLocation", factoryLocation);

            return ResponseEntity.ok(resp);

        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        Object userId = session.getAttribute("teago_userId");
        Object role = session.getAttribute("teago_role");

        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No active session"));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("userId", session.getAttribute("teago_userId"));
        resp.put("role", session.getAttribute("teago_role"));
        resp.put("fullName", session.getAttribute("teago_fullName"));
        resp.put("username", session.getAttribute("teago_username"));
        resp.put("landOwnerId", session.getAttribute("teago_landOwnerId"));
        resp.put("landSize", session.getAttribute("teago_landSize"));
        resp.put("factoryOwnerId", session.getAttribute("teago_factoryOwnerId"));
        resp.put("factoryName", session.getAttribute("teago_factoryName"));
        resp.put("factoryLocation", session.getAttribute("teago_factoryLocation"));

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}