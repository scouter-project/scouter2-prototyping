package scouter2.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class FileUtil {

	public static DatagramSocket close(DatagramSocket udp){
		try {
			if (udp != null) {
				udp.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}
	public static InputStream close(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static OutputStream close(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static Reader close(Reader in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static Writer close(Writer out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static byte[] readAll(InputStream fin) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		int n = fin.read(buff);
		while (n >= 0) {
			out.write(buff, 0, n);
			n = fin.read(buff);
		}
		return out.toByteArray();
	}

	public static RandomAccessFile close(RandomAccessFile raf) {
		try {
			if (raf != null) {
				raf.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static Socket close(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static ServerSocket close(ServerSocket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static File saveAsTemp(String prefix, String postfix, byte[] b) throws IOException {
		File file = File.createTempFile(prefix, postfix);
		file.deleteOnExit();
		save(file, b);
		return file;
	}

	public static void save(String file, byte[] b) {
		save(new File(file), b);

	}

	public static void save(File file, byte[] byteArray) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(byteArray);
		} catch (Exception e) {
		}
		close(out);
	}

	public static boolean saveText(File file, String contents) {
		OutputStream out = null;
		try {
			if (file.getParentFile().exists() == false) {
				file.getParentFile().mkdirs();
			}
			out = new FileOutputStream(file);
			out.write(contents.getBytes());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(out);
		}
		return false;
	}

	public static byte[] readAll(File file) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			return readAll(in);
		} catch (Exception e) {
		} finally {
			close(in);
		}
		return null;
	}

	public static void copy(File src, File dest) {
		try {
			copy(src, dest, true);
		} catch (Exception e) {
		}
	}

	public static boolean copy(File src, File dest, boolean overwrite) throws IOException {

		if (!src.isFile() || !src.exists())
			return false;

		if (dest.exists()) {
			if (dest.isDirectory()) // Directory이면 src파일명을 사용한다.
				dest = new File(dest, src.getName());
			else if (dest.isFile()) {
				if (!overwrite)
					throw new IOException(dest.getAbsolutePath() + "' already exists!");
			} else
				throw new IOException("Invalid  file '" + dest.getAbsolutePath() + "'");
		}

		File destDir = dest.getParentFile();
		if (!destDir.exists())
			if (!destDir.mkdirs())
				throw new IOException("Failed to create " + destDir.getAbsolutePath());
		long fileSize = src.length();
		if (fileSize > 20 * 1024 * 1024) {
			FileInputStream in = null;
			FileOutputStream out = null;
			try {
				in = new FileInputStream(src);
				out = new FileOutputStream(dest);
				int done = 0;
				int buffLen = 32768;
				byte buf[] = new byte[buffLen];
				while ((done = in.read(buf, 0, buffLen)) >= 0) {
					if (done == 0)
						Thread.yield();
					else
						out.write(buf, 0, done);
				}
			} finally {
				close(in);
				close(out);
			}
		} else {
			FileInputStream in = null;
			FileOutputStream out = null;
			FileChannel fin = null;
			FileChannel fout = null;
			try {
				in = new FileInputStream(src);
				out = new FileOutputStream(dest);
				fin = in.getChannel();
				fout = out.getChannel();

				long position = 0;
				long done = 0;
				long count = Math.min(65536, fileSize);
				do {
					done = fin.transferTo(position, count, fout);
					position += done;
					fileSize -= done;
				} while (fileSize > 0);
			} finally {
				close(fin);
				close(fout);
				close(in);
				close(out);
			}
		}
		return true;
	}

	public static FileChannel close(FileChannel fc) {
		if (fc != null) {
			try {
				fc.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static String getJarLocation(Class class1) {
		try {
			String path = "" + class1.getResource("/" + class1.getName().replace('.', '/') + ".class");
			if (path.indexOf("!") < 0)
				return null;
			path = path.substring("jar:file:".length(), path.indexOf("!"));
			path = path.substring(0, path.lastIndexOf('/'));
			return new File(path).getAbsolutePath();
		} catch (Exception e) {
		}
		return null;
	}
	public static String getJarFileName(Class class1) {
		try {
			String path = "" + class1.getResource("/" + class1.getName().replace('.', '/') + ".class");
			if (path.indexOf("!") < 0)
				return null;
			path = path.substring("jar:file:".length(), path.indexOf("!"));
			return new File(path).getAbsolutePath();
		} catch (Exception e) {
		}
		return null;
	}
	public static void main(String[] args) throws IOException {
		String path = getJarLocation(FileUtil.class);
		System.out.println(path);
		new File(path, "test.txt").createNewFile();
		System.out.println(new File(path).canWrite());
		System.out.println(new File(path).getAbsolutePath());
	}

	public static String load(File file, String enc) {
		if (file == null || !file.canRead())
			return null;
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			return new String(readAll(in), enc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(in);
		}
		return null;
	}

	public static void append(String file, String line) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file, true));
			out.println(line);
		} catch (Exception e) {
		}
		close(out);
	}

	public static boolean mkdirs(String path) {
		File f = new File(path);
		if (f.exists() == false)
			return f.mkdirs();
		else
			return true;
	}

	public static Properties readProperties(File f) {
		BufferedInputStream reader = null;
		Properties p = new Properties();
		try {
			reader = new BufferedInputStream(new FileInputStream(f));
			p.load(reader);
		} catch (Exception e) {
		} finally {
			close(reader);
		}
		return p;
	}

	public static void writeProperties(File f, Properties p) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
			p.list(pw);
		} catch (Exception e) {
		} finally {
			close(pw);
		}

	}
}