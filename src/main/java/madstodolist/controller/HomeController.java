package madstodolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;

import javax.servlet.http.HttpSession;


@Controller
public class HomeController {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/about") 
    public String about(Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        Usuario usuario = (idUsuarioLogeado != null) ? usuarioService.findById(idUsuarioLogeado)
                : null;
        model.addAttribute("usuario", usuario);
        return "about";
    }

}