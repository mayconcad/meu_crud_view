package br.com.sts.ddum.view.uploads;

import java.io.IOException;
import java.io.InputStream;

import org.primefaces.model.UploadedFile;

public class UploadedFileUtil implements UploadedFile {

	private long size;

	private String fileName;

	private InputStream inputStream;

	private byte[] contents;

	private String contentType;

	public UploadedFileUtil() {
	}

	/**
	 * @param size
	 * @param fileName
	 * @param inputStream
	 * @param contents
	 * @param contentType
	 */
	public UploadedFileUtil(long size, String fileName,
			InputStream inputStream, byte[] contents, String contentType) {
		super();
		this.size = size;
		this.fileName = fileName;
		this.inputStream = inputStream;
		this.contents = contents;
		this.contentType = contentType;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public InputStream getInputstream() throws IOException {
		return inputStream;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public byte[] getContents() {
		return contents;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void write(String arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}