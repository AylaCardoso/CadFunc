package com.empresa.servlets;

import com.empresa.util.SemImagem;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/imagem-padrao")
public class ImagemPadraoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        byte[] imagemBytes = SemImagem.gerar();
        resp.setContentType("image/png");
        resp.getOutputStream().write(imagemBytes);
    }
}
