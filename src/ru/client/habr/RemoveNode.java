package ru.client.habr;

/**
 * @author WNeZRoS
 * ������� �������� HTML ��������
 */
public final class RemoveNode {
	
	/**
	 * ����� ��� ��� ����������� �������� � ������
	 * @param data ����� HTML ��������
	 * @return ����� ��� ��������
	 */
	public static String removeImage(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<img[^>]+src=\"([^\"]+)\"[^>]+>", 
				"<h4 class=\"ufo\" onClick=\"this.innerHTML='<img src=\\\\'$1\\\\'>';\">Здесь была картинка</h4>");
	}
	
	/**
	 * ������ ����� ������ �� ������
	 * @param data ����� HTML ��������
	 * @return ����� ��� �����
	 */
	public static String removeVideo(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<object.+<param name=\"(movie|video)\" value=\"([^\"]+)\".+</object>", 
				"<h3><a href=\"$2\">Здесь был ролик</a></h3>");
	}
}
