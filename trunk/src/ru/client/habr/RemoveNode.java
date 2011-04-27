package ru.client.habr;

/**
 * @author WNeZRoS
 * Удаляет элементы HTML страницы
 */
public final class RemoveNode {
	
	/**
	 * Вызов НЛО для уничтожения картинок в тексте
	 * @param data текст HTML страницы
	 * @return текст без картинок
	 */
	public static String removeImage(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<img[^>]+src=\"([^\"]+)\"[^>]+>", "<h4 class=\"ufo\">Прилитело НЛО и уничтожило <a href=\"$1\">картинку</a></h4>");
	}
	
	/**
	 * Замена видео ролика на ссылку
	 * @param data текст HTML страницы
	 * @return текст без видео
	 */
	public static String removeVideo(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<object.+<embed src=\"([^\"]+)\".+</object>", "<a href=\"$1\">Здесь был ролик</a>");
	}
}
