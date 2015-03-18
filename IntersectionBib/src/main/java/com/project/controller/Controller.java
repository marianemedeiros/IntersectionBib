/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.controller;

import com.project.gui.FileChoose;
import com.project.intersectionbib.IntersectionBib;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import net.sf.jabref.BibtexEntry;

/**
 *
 * @author mariane
 */
public class Controller {
    
    private File file1;
    private File file2;
    public ArrayList<BibtexEntry> intersecao;
    
    public Controller(File file1, File file2){
        this.file1 = file1;
        this.file2 = file2;
    }
    
    public void getIntersectionBib(){
        IntersectionBib in = new IntersectionBib(this.file1, this.file2);
            in.readData();
            this.intersecao = in.verifyIntersection();
    }

    private void showIntersection() {
        String context = "";
        FileChoose f = new FileChoose();
        if(this.intersecao.isEmpty()){
              f.showMessage("Não há interseção entre as bases", this.intersecao.size());
        }else{
            for (BibtexEntry entry : intersecao) {
                context = context.concat(entry.getField("title").concat("\n"));
            }
            f.showMessage(context, this.intersecao.size());
        }
    }
}
