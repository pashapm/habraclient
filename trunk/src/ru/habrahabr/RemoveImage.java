package ru.habrahabr;

public class RemoveImage 
{
	/**
	 * Вызов НЛО для уничтожения картинок в тексте
	 * @param data текст
	 * @return текст без картинок
	 */
	public static String remove(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<img[^>]+src=\"([^\"]+)\"[^>]+>", "<h4 class=\"ufo\">Прилитело НЛО и уничтожило <a href=\"$1\">картинку</a></h4>");
	}
}
