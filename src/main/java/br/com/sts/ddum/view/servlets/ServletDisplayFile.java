package br.com.sts.ddum.view.servlets;

import java.io.IOException;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lowagie.text.pdf.codec.Base64.InputStream;
import com.mysql.jdbc.Statement;

@WebServlet(name = "DisplayFile", urlPatterns = { "/DisplayFile" })
public class ServletDisplayFile extends HttpServlet {
	private static final long serialVersionUID = 4593558495041379082L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ServletOutputStream outputStream = null;

		try {
			HttpSession session = request.getSession(true);

			//
			byte[] contentType = (byte[]) session.getAttribute("contentType");

			Statement stmt = null;
			ResultSet rs;
			InputStream sImage;

			String id = request.getParameter("Image_id");
			System.out.println("inside servlet–>" + id);

			// Connection con = Database.getConnection();
			// stmt = con.createStatement();
			// String strSql = "select image from upload_image where image_id='"
			// + id + "' ";
			// rs = stmt.executeQuery(strSql);
			// if (rs.next()) {
			byte[] bytearray = new byte[1048576];
			int size = 0;
			// sImage = rs.getBinaryStream(1);
			response.reset();
			response.setContentType("image/jpeg");
			// while ((size = sImage.read(bytearray)) != -1) {
			// response.getOutputStream().
			// write(bytearray, 0, size);
			// }
			// }

			response.getOutputStream().write(contentType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
