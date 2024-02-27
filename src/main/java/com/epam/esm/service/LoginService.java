package com.epam.esm.service;

import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

//  aqui veo si el usuario se puede autenticar o no.
//  recibo el username y la contrasena sin encriptar, dos strings
  public boolean checkAuthentication(String username, String password) {

    //Veo si el usuario con ese username existe, lo busco.
    User user = userRepository.findByUsername(username);

    //si existe, no es null, compruebo si la version sin codificar (password)
    // y la version codificada (user.getPassword) coinciden
    //este metodo es 	boolean matches(CharSequence rawPassword, String encodedPassword);

    if (user != null) {
      return passwordEncoder.matches(password, user.getPassword());
    }

    //si la contrasena es correcta, devuelve true, en cualquier otro caso devuelve false.
    return false;

  }
}
