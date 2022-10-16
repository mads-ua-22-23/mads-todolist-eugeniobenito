package madstodolist.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model, HttpSession session) {
        model.addAttribute("usuarios", usuarioService.allUsuarios());
        return "listaUsuarios";
    }
    
}
