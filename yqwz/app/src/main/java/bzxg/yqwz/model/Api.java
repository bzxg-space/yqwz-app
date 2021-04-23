package bzxg.yqwz.model;

import java.io.Serializable;

/**
 * Created by bzxg on 2021/3/7.
 */
public class Api implements Serializable {
    private String name;//名称
    private String url;//地址
    public Api() {
    }

    public Api(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
