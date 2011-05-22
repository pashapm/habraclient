package ru.client.habr;

import ru.client.habr.R;

/**
 * @author WNeZRoS
 * Удаляет элементы HTML со страницы
 */
public final class RemoveNode {
	
	/**
	 * Заменяет картинки на надпись и JavaScript для показа по клику
	 * @param data Код HTML страницы
	 * @return Стрница без картинок
	 */
	public static String removeImage(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<img[^>]+src=\"([^\"]+)\"[^>]+>", 
				"<h4 class=\"ufo\" onClick=\"this.innerHTML='<img src=\\\\'$1\\\\'>';\">" 
				+ ActivityMain.getStringFromResource(R.string.this_is_picture) + "</h4>");
	}
	
	/**
	 * Заменяет видео на ссылку на ролик. Может заменять видео с Youtube, Vimeo, Yandex Video
	 * @param data Код HTML страницы
	 * @return Страница без видео
	 */
	public static String removeVideo(String data)
	{
		if(data == null) return null;
		return data.replaceAll("<object.+<param name=\"(movie|video)\" value=\"([^\"]+)\".+</object>", 
				"<h3><a href=\"$2\">" + ActivityMain.getStringFromResource(R.string.this_is_video) + "</a></h3>");
	}
}
