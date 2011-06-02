package ru.client.habr;

import java.util.regex.Pattern;

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
				"<h3><a href=\"$2\">" + ActivityMain.getStringFromResource(R.string.this_is_video) 
				+ "</a></h3>").replaceAll("<iframe.+src=\"([^\"]+)\".+</iframe>", "<h3><a href=\"$1\">" 
						+ ActivityMain.getStringFromResource(R.string.this_is_video) + "</a></h3>");
	}
	
	public static String removeAudio(String data) {
		if(data == null) return null;
		
		return Pattern.compile("<script .+\\('song_url', \"([^\"]+)\"\\);.+</script>", 
				Pattern.DOTALL).matcher(data)
				.replaceAll("<a href=\"http://www.google.ru/url?sa=t&source=web&url=$1\" class=\"podcast\">Podcast</a>");
	}
	
	public static String replaceAudioScriptToFlash(String data) {
		if(data == null) return null;
		
		return Pattern.compile("<script .+\\('song_url', \"([^\"]+)\"\\);.+</script>", 
				Pattern.DOTALL).matcher(data)
				.replaceAll("<embed type=\"application/x-shockwave-flash\" " +
						"src=\"http://habrahabr.ru/i/rpod-plaeyr_shielded_secure.swf\" " +
						"width=\"98\" height=\"20\" id=\"hvp\" name=\"hvp\" bgcolor=\"#ffffff\" " +
						"quality=\"high\" salign=\"tl\" scale=\"noscale\" flashvars=\"song_url=$1\">");
	}
	
	public static String removeMultimedia(String data) {
		return removeVideo(removeAudio(data));
	}
}
