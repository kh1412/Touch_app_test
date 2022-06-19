package com.example.touch_app_test;

import java.util.ArrayList;

public class Kana_Character {
    String kana[][] = {{"あ","い","う","え","お"},
            {"か","き","く","け","こ"},
            {"さ","し","す","せ","そ"},
            {"た","ち","つ","て","と"},
            {"な","に","ぬ","ね","の"},
            {"は","ひ","ふ","へ","ほ"},
            {"ま","み","む","め","も"},
            {"や","","ゆ","","よ"},
            {"ら","り","る","れ","ろ"},
            {"わ","を","ん","",""},
            {"記","小","゛","゜",""},
            {"他","確定","削除","空白",""}};

    void Kana_Character(){

    }

    String set(int i, int j){
        return kana[i][j];
    }
}
