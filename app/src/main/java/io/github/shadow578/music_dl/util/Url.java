package io.github.shadow578.music_dl.util;

import androidx.annotation.NonNull;

/**
 * url constants
 */
public enum Url {

    /**
     * the main youtube music page link
     */
    YoutubeMusicMain("https://music.youtube.com");

    /**
     * the url of this value
     */
    @NonNull
    private final String url;

    /**
     * create a new url value
     * @param url the url
     */
    Url(@NonNull String url)
    {
        this.url = url;
    }

    /**
     * @return the url of this value
     */
    public String url(){
        return url;
    }
}
