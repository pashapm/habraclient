package ru.client.habr;

public class RemoveNode 
{
	/**
	 * Вызов НЛО для уничтожения картинок в тексте
	 * @param data текст
	 * @return текст без картинок
	 */
	public static String removeImage(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<img[^>]+src=\"([^\"]+)\"[^>]+>", "<h4 class=\"ufo\">Прилитело НЛО и уничтожило <a href=\"$1\">картинку</a></h4>");
	}
	
	public static String removeVideo(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<object.+<embed src=\"([^\"]+)\".+</object>", "<a href=\"$1\">Здесь был ролик</a>");
	}
}
