package tk.ziniulian.util;

/**
 * 字符转换
 * Created by 李泽荣 on 2018/7/18.
 */

public class Str {
	private static final String H2BD = "0123456789ABCDEF";

	// 将十六进制字符串转换为二进制字节数组
	public static byte[] Hexstr2Bytes (String hs) {
		if (hs == null) {
			return new byte[0];
		}
		double n = hs.length();
		int nSize = (int)Math.floor(n / 2.0D);
		if (nSize <= 0) {
			return new byte[0];
		}
		n = 2 * nSize;
		byte[] bs = new byte[nSize];
		int u, d;
		for (int i = 0; i < n; i += 2) {
			/*
			// 该转换方式存在问题，默认只能转换 -128 ～ 127 之间的数字。
			String s = "" + dat.charAt(i) + dat.charAt(i+1);
			bs[i/2] = Byte.parseByte(s, 16);
			*/

			// 新的十六进制字符转换方式：
			//			u = H2BD.indexOf(hs.charAt(i));
			//			if (u == -1) {
			//				u = 0;
			//			} else {
			//				d = H2BD.indexOf(hs.charAt(i+1));
			//				if (d == -1) {
			//					u = 0;
			//				} else {
			//					u = u * 16 + d;
			//				}
			//			}
			//			bs[i/2] = (byte)u;
			bs[i/2] = (byte) (H2BD.indexOf(hs.charAt(i)) * 16 + H2BD.indexOf(hs.charAt(i+1)));
		}
		return bs;
	}

	// 将二进制字节数组转换为十六进制字符串
	public static String Bytes2Hexstr (byte[] bs) {
		if (bs == null) {
			return "";
		}
		StringBuilder r = new StringBuilder();
		String s;
		int n;
		for (n=0; n<bs.length; n++) {
			// s = Integer.toString(bs[n], 16);	// 该方法会将大于 127 的数转换为负值
			s = Integer.toHexString(bs[n] & 0xFF);
			if (s.length() < 2) {
				r.append('0');
			}
			r.append(s);
		}
		return r.toString().toUpperCase();
	}

	// 将二进制字节数组转换成字符数据
	public static String Bytes2Dat(byte[] bs) {
		try {
			return new String(bs, "UTF-8").trim();
		} catch (Exception e) {
			return "";
		}
	}

	// 将二进制字节数组转换成ASCII字符数据
	public static String Bytes2Asc(byte[] bs) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < bs.length; i ++) {
			int b = bs[i];
			if (b > 31 && b < 127) {
				r.append((char)b);
			} /*else if (b != 0) {
				r.append('?');
			}*/
		}
		return r.toString();
	}

	// 修剪乱码
	public static String trimGarbled (String s) {
		StringBuilder r = new StringBuilder();
		char[] cs= s.toCharArray();
		for (int i = 0; i < cs.length; i ++) {
			char c = cs[i];
			if ((c > 31 && c != 127) || c == 9 || c == 10 || c == 13) {
				r.append(c);
			}
		}
		return r.toString();
	}

	// 将十六进制字符串转换成字符数据
	public static String Hexstr2Dat(String hs) {
		return Bytes2Dat(Hexstr2Bytes(hs));
	}

	// 将字符数据转换成十六进制字符串
	public static String Dat2Hexstr (String dat) {
		try {
			return Bytes2Hexstr(dat.getBytes("UTF-8"));
		} catch (Exception e) {
			return "";
		}
	}

	// 将字符数据转换成指定长度的十六进制字符串
	public static String Dat2Hexstr (String dat, int size) {
		return Bytes2Hexstr(getLimitBytes(dat, size));
	}

	// 获取字符串限定长度的二进制数组
	public static byte[] getLimitBytes (String dat, int limit) {
		byte[] r = new byte[limit];
		try {
			byte[] bs;
			int n = dat.length();
			if (n > limit) {
				n = limit;
				bs = dat.substring(0, n).getBytes("UTF-8");
			} else {
				bs = dat.getBytes();
			}
			while (bs.length > limit) {
				n --;
				bs = dat.substring(0, n).getBytes("UTF-8");
			}
			System.arraycopy(bs, 0, r, 0, bs.length);
		} catch (Exception e) {
			//			e.printStackTrace();
		}
		return r;
	}

	// 将字符数据转换成二进制数组
	public static byte[] Dat2Bytes (String dat) {
		try {
			return dat.getBytes("UTF-8");
		} catch (Exception e) {
			return new byte[0];
		}
	}

	// 字符合并
	public static String meg (String txt, String... args) {
		if (args != null) {
			for (int i = 0; i < args.length; i ++) {
				txt = txt.replaceAll(("<" + i + ">"), args[i]);
			}
		}
		return txt;
	}

	public static void main (String[] args) {
		// 测试Str类方法是否会报错
		System.out.println("|" + Str.Hexstr2Bytes(null).length + "|");
		System.out.println("|" + Str.Hexstr2Bytes("").length + "|");
		System.out.println("|" + Str.Bytes2Hexstr(null) + "|");
		System.out.println("|" + Str.Bytes2Hexstr(new byte[0]) + "|");
		System.out.println("|" + Str.Bytes2Hexstr(Str.Hexstr2Bytes("hk0p409")) + "|");
		System.out.println("|" + Str.Bytes2Dat(null) + "|");
		System.out.println("|" + Str.Bytes2Dat(new byte[0]) + "|");
		System.out.println("|" + "".getBytes().length + "|");
		System.out.println("|" + Str.Hexstr2Dat(null) + "|");
		System.out.println("|" + Str.Hexstr2Dat("") + "|");
		System.out.println("|" + Str.Hexstr2Dat("613041") + "|");
		System.out.println("|" + Str.Dat2Hexstr(null) + "|");
		System.out.println("|" + Str.Dat2Hexstr("") + "|");
		System.out.println("|" + Str.Dat2Hexstr("你好") + "|");
		System.out.println("|" + "aaa".substring(0, 0) + "|");
		System.out.println("|" + Str.Dat2Hexstr("你好", 5) + "|");
	}
}
