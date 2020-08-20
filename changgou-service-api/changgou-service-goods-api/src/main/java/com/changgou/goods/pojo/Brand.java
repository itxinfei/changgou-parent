package com.changgou.goods.pojo;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 */
@Table(name = "tb_brand")
public class Brand implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;//品牌id

    @Column(name = "name")
    private String name;//品牌名称

    @Column(name = "image")
    private String image;//品牌图片地址

    @Column(name = "letter")
    private String letter;//品牌的首字母

    @Column(name = "seq")
    private Integer seq;//排序

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }


}
