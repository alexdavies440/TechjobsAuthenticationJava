package org.launchcode.techjobsauth.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.launchcode.techjobsauth.models.User;
import org.launchcode.techjobsauth.models.data.UserRepository;
import org.launchcode.techjobsauth.models.dto.LoginFormDTO;
import org.launchcode.techjobsauth.models.dto.RegisterFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class AuthenticationController {
    @Autowired
    UserRepository userRepository;

    // 1.
    private static final String userSessionKey = "user";

    // 2.
    public User getFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(userSessionKey);
        if (userId == null) {
            return  null;
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return null;
        }

        return user.get();
    }

    // 3.
    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

    // 4.1
    @GetMapping("/register")
    public String displayRegisterForm(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute(new RegisterFormDTO());
        return "register";
    }

    // 4.3
    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
                                          HttpServletRequest request,
                                          Errors errors,
                                          Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            return "register";
        }

        User existsingUser = userRepository.findByUsername(registerFormDTO.getUsername());

        if (existsingUser != null) {
            errors.rejectValue("username",
                    "username.alreadyexists",
                    "A user with that username already exists. Try being more creative?");
            model.addAttribute("title", "Register");
            return "register";
        }

        String password = registerFormDTO.getPassword();
        String verifyPassword = registerFormDTO.getVerifyPassword();
        if (!password.equals(verifyPassword)) {
            errors.rejectValue("password",
                    "passwords.mismatch",
                    "Whoa there! Passwords don't match");
            model.addAttribute("title", "Register");
            return "register";
        }

        User newUser = new User(registerFormDTO.getUsername(), registerFormDTO.getPassword());
        userRepository.save(newUser);
        setUserInSession(request.getSession(), newUser);

        return "redirect:";
    }

    @GetMapping("/login")
    public String displayLoginForm(Model model) {
        model.addAttribute("title", "Log In");
        model.addAttribute(new LoginFormDTO());
        return "login";
    }

    // 5.
    @PostMapping("/login")
    public String login(@ModelAttribute @Valid LoginFormDTO loginFormDTO,
                        Errors errors,
                        HttpServletRequest request,
                        Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Log In");
            return "login";
        }

        User theUser = userRepository.findByUsername(loginFormDTO.getUsername());

        if (theUser == null) {
            errors.rejectValue("username",
                    "user.invalid",
                    "WHO ARE YOU??!");
        }

        String password = loginFormDTO.getPassword();

        if (!theUser.isMatchingPassword(password)) {
            errors.rejectValue("password",
                    "password.invalid",
                    "Invalid password");
            model.addAttribute("title", "Log In");
            return "login";
        }

        setUserInSession(request.getSession(), theUser);

        return "redirect:";
    }

    // 6.
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
