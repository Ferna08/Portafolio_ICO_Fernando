/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tienda.service.impl;

/**
 *
 * @author ferna
 */
public class UsuarioServiceImpl {
    
}
Recorte001 UsuarioDao.java
==========

package com.tienda.dao;

import com.tienda.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDao extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String username);
    
    Usuario findByUsernameAndPassword(String username, String Password);

    Usuario findByUsernameOrCorreo(String username, String correo);

    boolean existsByUsernameOrCorreo(String username, String correo);
}


Recorte002 RolDao.java
==========
package com.tienda.dao;

import com.tienda.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolDao extends JpaRepository<Rol, Long> {

}


Recorte003 UsuarioService.java
==========

package com.tienda.service;

import com.tienda.domain.Usuario;
import java.util.List;

public interface UsuarioService {
    
    // Se obtiene un listado de usuarios en un List
    public List<Usuario> getUsuarios();
    
    // Se obtiene un Usuario, a partir del id de un usuario
    public Usuario getUsuario(Usuario usuario);
    
    // Se obtiene un Usuario, a partir del username de un usuario
    public Usuario getUsuarioPorUsername(String username);

    // Se obtiene un Usuario, a partir del username y el password de un usuario
    public Usuario getUsuarioPorUsernameYPassword(String username, String password);
    
    // Se obtiene un Usuario, a partir del username y el password de un usuario
    public Usuario getUsuarioPorUsernameOCorreo(String username, String correo);
    
    // Se valida si existe un Usuario considerando el username
    public boolean existeUsuarioPorUsernameOCorreo(String username, String correo);
    
    // Se inserta un nuevo usuario si el id del usuario esta vacío
    // Se actualiza un usuario si el id del usuario NO esta vacío
    public void save(Usuario usuario,boolean crearRolUser);
    
    // Se elimina el usuario que tiene el id pasado por parámetro
    public void delete(Usuario usuario);
    
}


Recorte004 UsuarioServiceImpl.java
==========

package com.tienda.service.impl;

import com.tienda.dao.RolDao;
import com.tienda.dao.UsuarioDao;
import com.tienda.domain.Rol;
import com.tienda.domain.Usuario;
import com.tienda.service.UsuarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private RolDao rolDao;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios() {
        return usuarioDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuario(Usuario usuario) {
        return usuarioDao.findById(usuario.getIdUsuario()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioPorUsername(String username) {
        return usuarioDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioPorUsernameYPassword(String username, String password) {
        return usuarioDao.findByUsernameAndPassword(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioPorUsernameOCorreo(String username, String correo) {
        return usuarioDao.findByUsernameOrCorreo(username, correo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsernameOCorreo(String username, String correo) {
        return usuarioDao.existsByUsernameOrCorreo(username, correo);
    }

    @Override
    @Transactional
    public void save(Usuario usuario, boolean crearRolUser) {
        usuario=usuarioDao.save(usuario);
        if (crearRolUser) {  //Si se está creando el usuario, se crea el rol por defecto "USER"
            Rol rol = new Rol();
            rol.setNombre("ROLE_USER");
            rol.setIdUsuario(usuario.getIdUsuario());
            rolDao.save(rol);
        }
    }

    @Override
    @Transactional
    public void delete(Usuario usuario) {
        usuarioDao.delete(usuario);
    }
}


Recorte005 UsuarioController.java
==========

package com.tienda.controller;

import com.tienda.domain.Usuario;
import com.tienda.service.UsuarioService;
import com.tienda.service.FirebaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/listado")
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "/usuario/listado";
    }

    @GetMapping("/nuevo")
    public String usuarioNuevo(Usuario usuario) {
        return "/usuario/modifica";
    }

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @PostMapping("/guardar")
    public String usuarioGuardar(Usuario usuario,
            @RequestParam("imagenFile") MultipartFile imagenFile) {
        if (!imagenFile.isEmpty()) {
            usuarioService.save(usuario,false);
            usuario.setRutaImagen(
                    firebaseStorageService.cargaImagen(
                            imagenFile,
                            "usuario",
                            usuario.getIdUsuario()));
        }
        usuarioService.save(usuario,true);
        return "redirect:/usuario/listado";
    }

    @GetMapping("/eliminar/{idUsuario}")
    public String usuarioEliminar(Usuario usuario) {
        usuarioService.delete(usuario);
        return "redirect:/usuario/listado";
    }

    @GetMapping("/modificar/{idUsuario}")
    public String usuarioModificar(Usuario usuario, Model model) {
        usuario = usuarioService.getUsuario(usuario);
        model.addAttribute("usuario", usuario);
        return "/usuario/modifica";
    }
}


Recorte006 usuario\fragmentos.html
==========

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>
    </head>
    <body>
        <!-- 1 Sección para crear el boton de agregar... llamará a una ventana modal-->
        <section th:fragment="botonesAgregar" class="py-4 mb-4 bg-light">
            <div class="container" sec:authorize="hasRole('ROLE_ADMIN')">
                <div class="row">
                    <div class="col-md-3">   
                        <button 
                            type="button" 
                            class="btn btn-primary btn-block" 
                            data-bs-toggle="modal" 
                            data-bs-target="#agregarUsuario">
                            <i class="fas fa-plus"></i> [[#{usuario.agregar}]]
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- 2 Fragmento para agregar usuario, es la ventana modal -->
        <section th:fragment="agregarUsuario">
            <div id="agregarUsuario" 
                 class="modal fade" 
                 tabindex="-1" 
                 aria-labelledby="exampleModalLabel" 
                 aria-hidden="true">
                <div class="modal-dialog modal-md">
                    <div class="modal-content">
                        <div class="modal-header bg-info text-white">
                            <h5 class="modal-title">[[#{usuario.agregar}]]</h5>
                            <button type="button" 
                                    class="btn-close" 
                                    data-bs-dismiss="modal" 
                                    aria-label="Close"></button>
                        </div> 
                        <form th:action="@{/usuario/guardar}" th:object="${usuario}"
                              method="POST" class="was-validated"
                              enctype="multipart/form-data">
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label for="nombre">[[#{usuario.nombre}]]</label>
                                    <input type="text" class="form-control" name="nombre" required="true"/>
                                </div>
                                <div class="mb-3">
                                    <label for="apellidos">[[#{usuario.apellidos}]]</label>
                                    <input type="text" class="form-control" name="apellidos" required="true"/>
                                </div>
                                <div class="mb-3">
                                    <label for="username">[[#{usuario.username}]]</label>
                                    <input type="text" class="form-control" name="username" required="true"/>
                                </div>
                                <div class="mb-3">
                                    <label for="password">[[#{usuario.password}]]</label>
                                    <input type="password" class="form-control" name="password" required="true"/>
                                </div>
                                <div class="mb-3">
                                    <label for="email">[[#{usuario.correo}]]</label>
                                    <input type="email" class="form-control" name="correo" required="true"/>
                                </div>
                                <div class="mb-3">
                                    <label for="telefono">[[#{usuario.telefono}]]</label>
                                    <input type="tel" class="form-control" name="telefono"/>
                                </div>
                                <div class="mb-3">
                                    <label for="imagen">[[#{usuario.imagen}]]</label>
                                    <input class="form-control" type="file" name="imagenFile"
                                           onchange="readURL(this);"/>
                                    <img id="blah" src="#" alt="your image" height="200"/>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button class="btn btn-primary" type="submit">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </section>

        <!-- 3 Sección principal para mostrar la informaccion de la entidad usuario -->
        <section th:fragment="listadoUsuarios" id="usuarios">
            <div class="container">
                <div class="row">
                    <div class="col-md-9">
                        <div class="card">
                            <div class="card-header">
                                <h4>[[#{usuario.listado}]]</h4></div>
                            <div th:if="${usuarios != null and !usuarios.empty}">
                                <table class="table table-striped table-hover">
                                    <thead class="table-dark">
                                        <tr><th>#</th>
                                            <th>[[#{usuario.username}]]</th>
                                            <th>[[#{usuario.nombre}]]</th>
                                            <th>[[#{usuario.apellidos}]]</th>
                                            <th>[[#{usuario.imagen}]]</th>
                                            <th></th></tr>
                                    </thead>
                                    <tbody>
                                        <tr th:each="usuario, contador : ${usuarios}">
                                            <td>[[${contador.count}]]</td>
                                            <td>[[${usuario.username}]]</td>
                                            <td>[[${usuario.nombre}]]</td>
                                            <td>[[${usuario.apellidos}]]</td>
                                            <td sec:authorize="hasRole('ROLE_ADMIN')">
                                                <a th:href="@{/usuario/eliminar/}+${usuario.idUsuario}"
                                                   class="btn btn-danger">
                                                    <i class="fas fa-trash"></i> [[#{accion.eliminar}]]</a>
                                                <a th:href="@{/usuario/modificar/}+${usuario.idUsuario}"
                                                   class="btn btn-success">
                                                    <i class="fas fa-pencil"></i> [[#{accion.actualizar}]]</a></td></tr>
                                    </tbody>
                                </table>
                            </div>
                            <div class="text-center p-2" th:if="${usuarios == null or usuarios.empty}">
                                <span>[[#{lista.vacia}]]</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card text-center bg-success text-white mb-3">
                            <div class="card-body">
                                <h3>[[#{usuario.total}]]</h3>
                                <h4 class="fs-2"><i class="fas fa-users"></i> [[${totalUsuarios}]]</h4>
                            </div>
                        </div>                        
                    </div>
                </div>
            </div>
        </section>

        <!-- 4 Fragmento que se utiliza en la página modifca.html -->
        <section th:fragment="editarUsuario">
            <div class="row">
                <div class="col-md-2"></div>
                <div class="col-md-8">
                    <form method="POST"
                          th:action="@{/usuario/guardar}" th:object="${usuario}"
                          class="was-validated"
                          enctype="multipart/form-data">
                        <input type="hidden" name="idUsuario" th:field="*{idUsuario}"/>
                        <section th:replace="~{usuario/fragmentos :: botonesEditar}"/>
                        <div id=details>
                            <div class="container">
                                <div class="row">
                                    <div class="col">
                                        <div class="card">
                                            <div class="card-header">
                                                <h4>[[#{accion.actualizar}]]</h4>
                                            </div>
                                            <div class="card-body">
                                                <div class="mb-3">
                                                    <label for="nombre">[[#{usuario.nombre}]]</label>
                                                    <input type="text" class="form-control" 
                                                           name="nombre" th:field="*{nombre}"
                                                           required="true">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="apellidos">[[#{usuario.apellidos}]]</label>
                                                    <input type="text" class="form-control" 
                                                           name="apellidos" th:field="*{apellidos}"
                                                           required="true">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="email">[[#{usuario.correo}]]</label>
                                                    <input type="email" class="form-control" 
                                                           name="correo" th:field="*{correo}"
                                                           required="true">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="telefono">[[#{usuario.telefono}]]</label>
                                                    <input type="tel" class="form-control" 
                                                           name="telefono" th:field="*{telefono}">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="imagen">[[#{usuario.imagen}]]</label>
                                                    <input class="form-control" type="file" name="imagenFile"
                                                           onchange="readURL(this);" />
                                                    <img id="blah" th:src="@{${usuario.rutaImagen}}" alt="your image" height="200"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                    </form>
                </div>
            </div>
        </section>

        <!-- 5 Fragmento que se utiliza en el fragmento anterior -->
        <section th:fragment="botonesEditar">
            <div class="container py-4 mb-4 bg-light">
                <div class="row">
                    <div class="col-md-4 d-grid">
                        <a th:href="@{/usuario/listado}" class="btn btn-primary">
                            <i class="fas fa-arrow-left"></i> [[#{accion.regresar}]]
                        </a>
                    </div>
                    <div class="col-md-4 d-grid" sec:authorize="hasRole('ROLE_ADMIN')">                                
                        <a th:href="@{/usuario/eliminar/}+${usuario.idUsuario}"
                           class="btn btn-danger">
                            <i class="fas fa-trash"></i> [[#{accion.eliminar}]]
                        </a>
                    </div>
                    <div class="col-md-4 d-grid" sec:authorize="hasRole('ROLE_ADMIN')">
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-check"></i> [[#{accion.guardar}]]
                        </button>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>


Recorte007 usuario\listado.html
==========

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"></header>

        <section th:replace="~{usuario/fragmentos :: botonesAgregar}"></section>
        <section th:replace="~{usuario/fragmentos :: agregarUsuario}"></section>
        <section th:replace="~{usuario/fragmentos :: listadoUsuarios}"></section>
        
        <footer th:replace="~{layout/plantilla :: footer}"></footer>

    </body>
</html>

Recorte008 usuario\modifica.html
==========

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"></header>

        <section th:replace="~{usuario/fragmentos :: editarUsuario}"></section>

        <footer th:replace="~{layout/plantilla :: footer}"></footer>

    </body>
</html>


Recorte009 pom.cml
==========

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-mail</artifactId>
</dependency>


Recorte010 CorreoService.java
==========

package com.tienda.service;

import jakarta.mail.MessagingException;

public interface CorreoService {
    public void enviarCorreoHtml(
            String para, 
            String asunto, 
            String contenidoHtml) 
            throws MessagingException;
}


Recorte011 CorreoServiceImpl.java
==========

package com.tienda.service.impl;

import com.tienda.service.CorreoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CorreoServiceImpl implements CorreoService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
      public void enviarCorreoHtml(
              String para, 
              String asunto, 
              String contenidoHtml) 
              throws MessagingException {
          
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper 
                = new MimeMessageHelper(message, 
                        true);
        helper.setTo(para);
        helper.setSubject(asunto);
        helper.setText(contenidoHtml,true);
        mailSender.send(message);
    }
}


Recorte012 appliaction.properties
==========

#Definiendo información para utilizar cuenta de Correo para usuarios
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ElCorreoDefinido@gmail.com
spring.mail.password=LaClave de 16 dígitos de gmail... la de fondo amarillo
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

#nombre del servidor para enviar el correo
servidor.http=localhost


Recorte013 RegistroService.java
==========




Recorte014 RegistroServiceImpl.java
==========

package com.tienda.service.impl;

import com.tienda.domain.Usuario;
import com.tienda.service.CorreoService;
import com.tienda.service.RegistroService;
import com.tienda.service.UsuarioService;
import jakarta.mail.MessagingException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RegistroServiceImpl implements RegistroService {

    @Autowired
    private CorreoService correoService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private MessageSource messageSource;  //creado en semana 4...
    @Autowired
    private FirebaseStorageServiceImpl firebaseStorageService;

    @Override
    public Model activar(Model model, String username, String clave) {
        Usuario usuario = 
                usuarioService.getUsuarioPorUsernameYPassword(username, 
                        clave);
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
        } else {
            model.addAttribute(
                    "titulo", 
                    messageSource.getMessage(
                            "registro.activar", 
                            null,  Locale.getDefault()));
            model.addAttribute(
                    "mensaje", 
                    messageSource.getMessage(
                            "registro.activar.error", 
                            null, Locale.getDefault()));
        }
        return model;
    }

    @Override
    public void activar(Usuario usuario, MultipartFile imagenFile) {
        var codigo = new BCryptPasswordEncoder();
        usuario.setPassword(codigo.encode(usuario.getPassword()));

        if (!imagenFile.isEmpty()) {
            usuarioService.save(usuario, false);
            usuario.setRutaImagen(
                    firebaseStorageService.cargaImagen(
                            imagenFile, 
                            "usuarios", 
                            usuario.getIdUsuario()));
        }
        usuarioService.save(usuario, true);
    }

    @Override
    public Model crearUsuario(Model model, Usuario usuario) 
            throws MessagingException {
        String mensaje;
        if (!usuarioService.
                existeUsuarioPorUsernameOCorreo(
                        usuario.getUsername(), 
                        usuario.getCorreo())) {
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);
            usuarioService.save(usuario, true);
            enviaCorreoActivar(usuario, clave);
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.activacion.ok", 
                            null, 
                            Locale.getDefault()),
                    usuario.getCorreo());
        } else {
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.usuario.o.correo", 
                            null, 
                            Locale.getDefault()),
                    usuario.getUsername(), usuario.getCorreo());
        }
        model.addAttribute(
                "titulo", 
                messageSource.getMessage(
                        "registro.activar", 
                        null, 
                        Locale.getDefault()));
        model.addAttribute(
                "mensaje", 
                mensaje);
        return model;
    }

    @Override
    public Model recordarUsuario(Model model, Usuario usuario) 
            throws MessagingException {
        String mensaje;
        Usuario usuario2 = usuarioService.getUsuarioPorUsernameOCorreo(
                usuario.getUsername(), 
                usuario.getCorreo());
        if (usuario2 != null) {
            String clave = demeClave();
            usuario2.setPassword(clave);
            usuario2.setActivo(false);
            usuarioService.save(usuario2, false);
            enviaCorreoRecordar(usuario2, clave);
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.recordar.ok", 
                            null, 
                            Locale.getDefault()),
                    usuario.getCorreo());
        } else {
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.usuario.o.correo", 
                            null, 
                            Locale.getDefault()),
                    usuario.getUsername(), usuario.getCorreo());
        }
        model.addAttribute(
                "titulo", 
                messageSource.getMessage(
                        "registro.activar", 
                        null, 
                        Locale.getDefault()));
        model.addAttribute(
                "mensaje", 
                mensaje);
        return model;
    }

    private String demeClave() {
        String tira = "ABCDEFGHIJKLMNOPQRSTUXYZabcdefghijklmnopqrstuvwxyz0123456789_*+-";
        String clave = "";
        for (int i = 0; i < 40; i++) {
            clave += tira.charAt((int) (Math.random() * tira.length()));
        }
        return clave;
    }

    //Ojo cómo le lee una informacion del application.properties
    @Value("${servidor.http}")
    private String servidor;

    private void enviaCorreoActivar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage(
                "registro.correo.activar", 
                null, Locale.getDefault());
        mensaje = String.format(
                mensaje, usuario.getNombre(), 
                usuario.getApellidos(), servidor, 
                usuario.getUsername(), clave);
        String asunto = messageSource.getMessage(
                "registro.mensaje.activacion", 
                null, Locale.getDefault());
        correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, mensaje);
    }

    private void enviaCorreoRecordar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage(""
                + "registro.correo.recordar", 
                null, 
                Locale.getDefault());
        mensaje = String.format(
                mensaje, usuario.getNombre(), 
                usuario.getApellidos(), servidor, 
                usuario.getUsername(), clave);
        String asunto = messageSource.getMessage(
                "registro.mensaje.recordar", 
                null, Locale.getDefault());
        correoService.enviarCorreoHtml(
                usuario.getCorreo(), 
                asunto, mensaje);
    }
}





Recorte016 registro\fragmentos.html
==========

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>
    </head>
    <body>
        <!-- 1 Fragmento que se utiliza en la página para regresar -->
        <section th:fragment="nuevoUsuario">
            <div class="row py-2 justify-content-center">
                <div class="col-md-3">
                    <section>
                        <div class="col-md-4 d-grid">
                            <a th:href="@{/login}" class="btn btn-primary">
                                <i class="fas fa-arrow-left"></i> [[#{accion.regresar}]]
                            </a>
                        </div>
                    </section>
                </div>
            <div class="row py-2 justify-content-center">
                <div class="col-md-3">
                    <form method="POST" th:action="@{/registro/crearUsuario}" th:object="${usuario}" class="was-validated">
                        <div class="card">
                            <div class="card-header">
                                <h4>[[#{usuario.agregar}]]</h4>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label for="username">[[#{usuario.username}]]</label>
                                    <input type="text" class="form-control" name="username" th:field="*{username}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="nombre">[[#{usuario.nombre}]]</label>
                                    <input type="text" class="form-control" name="nombre" th:field="*{nombre}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="apellidos">[[#{usuario.apellidos}]]</label>
                                    <input type="text" class="form-control" name="apellidos" th:field="*{apellidos}"
                                           required="true"/></div>
                                <div class="mb-3">
                                    <label for="email">[[#{usuario.correo}]]</label>
                                    <input type="email" class="form-control" 
                                           name="correo" th:field="*{correo}"
                                           required="true"/></div></div>
                            <div class="card-footer text-end">                                                
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-envelope"></i> [[#{usuario.activacion}]]
                                </button></div>
                        </div>
                    </form>
                </div>
            </div>
        </section>

        <!-- 2 Fragmento que se utiliza activar un usuario -->
        <section th:fragment="activarUsuario">
            <div class="row py-2 justify-content-center">
                <div class="col-md-3">
                    <form method="POST" th:action="@{/registro/activar}" th:object="${usuario}" class="was-validated" enctype="multipart/form-data">
                        <input type="hidden" name="idUsuario" th:field="*{idUsuario}"/>
                        <div class="card">
                            <div class="card-header align-items-center">
                                <h4>[[#{registro.activar}]]</h4>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label for="username">[[#{usuario.username}]]</label>
                                    <input type="text" class="form-control" name="username" th:field="*{username}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="nombre">[[#{usuario.nombre}]]</label>
                                    <input type="text" class="form-control" name="nombre" th:field="*{nombre}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="apellidos">[[#{usuario.apellidos}]]</label>
                                    <input type="text" class="form-control" name="apellidos" th:field="*{apellidos}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="email">[[#{usuario.correo}]]</label>
                                    <input type="email" class="form-control" name="correo" th:field="*{correo}" required="true"/></div>
                                <div class="mb-3">
                                    <label for="telefono">[[#{usuario.telefono}]]</label>
                                    <input type="tel" class="form-control" name="telefono" th:field="*{telefono}"/></div>
                                <div class="mb-3">
                                    <label for="password">[[#{usuario.password}]]</label>
                                    <input type="password" class="form-control" name="password" th:field="*{password}"/></div>
                                <div class="mb-3">
                                    <label for="imagen">[[#{usuario.imagen}]]</label>
                                    <input class="form-control" type="file" name="imagenFile" onchange="readURL(this);" />
                                    <img id="blah" th:src="@{${usuario.rutaImagen}}" alt="your image" height="200"/>
                                </div>                                                
                            </div>
                            <div class="card-footer align-items-center">
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-check"></i> [[#{usuario.activacion}]]
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </section>

        <!-- 3 Fragmento que se utiliza recordar un usuario -->
        <section th:fragment="recordarUsuario">
            <div class="row py-2 justify-content-center">
                <div class="col-md-3">
                    <section>
                        <div class="col-md-4 d-grid">
                            <a th:href="@{/login}" class="btn btn-primary">
                                <i class="fas fa-arrow-left"></i> [[#{accion.regresar}]]
                            </a>
                        </div>
                    </section>
                </div>
            </div>
            <div class="row py-2 justify-content-center">
                <div class="col-md-3">
                    <form method="POST"
                          th:action="@{/registro/recordarUsuario}" th:object="${usuario}"
                          class="was-validated">
                        <div class="card">
                            <div class="card-header">
                                <h4>[[#{registro.recordar.us}]]</h4>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label for="username">[[#{usuario.username}]]</label>
                                    <input type="text" class="form-control" name="username" th:field="*{username}"/></div>                                                
                                <div class="mb-3">
                                    <label for="email">[[#{usuario.correo}]]</label>
                                    <input type="email" class="form-control" name="correo" th:field="*{correo}"/></div>
                            </div>
                            <div class="card-footer text-end">                                                
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-address-card"></i> [[#{registro.recordar.us}]]
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </section>
    </body>
</html>


Recorte017 registro\activa.html
==========

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>        
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"/>

        <section th:replace="~{registro/fragmentos :: activarUsuario}"/>

        <footer th:replace="~{layout/plantilla :: footer}"/>
    </body>
</html>


Recorte018 registro\nuevo.html
==========

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>        
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"/>

        <section th:replace="~{registro/fragmentos :: nuevoUsuario}"/>

        <footer th:replace="~{layout/plantilla :: footer}"/>
    </body>
</html>

Recorte019 registro\recordar.html
==========

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>        
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"/>

        <section th:replace="~{registro/fragmentos :: recordarUsuario}"/>

        <footer th:replace="~{layout/plantilla :: footer}"/>
    </body>
</html>

Recorte020 registro\salida.html
==========

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
    <head th:replace="~{layout/plantilla :: head}">
        <title>TechShop</title>        
    </head>
    <body>
        <header th:replace="~{layout/plantilla :: header}"/>
        <div class="row py-2 justify-content-center">
            <div class="col-md-3">
                <div class="card">
                    <div class="card-header align-items-center">
                        [[${titulo}]]
                    </div>
                    <div class="card-body">
                        <strong>[[${mensaje}]]</strong>
                    </div>
                </div>
            </div>
        </div>
        <footer th:replace="~{layout/plantilla :: footer}"/>
    </body>
</html>