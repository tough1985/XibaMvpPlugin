package me.xiba

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.text.SimpleDateFormat;

class XibaMvpTask extends DefaultTask {
    //Contract模板
    def mvpContractTemplateText = '''package com.laoyuegou.android.${packageName}.contract;

import com.laoyuegou.android.lib.mvp.BaseMvp.MvpPresenter;
import com.laoyuegou.android.lib.mvp.BaseMvp.MvpView;

/**
* date: ${date}.
*/

public interface ${functionName}Contract {

    interface View extends MvpView {

    }

    interface Presenter extends MvpPresenter<View> {
        
    }
}
'''
    //Fragment模板
    def mvpFragmentTemplateText = '''package com.laoyuegou.android.${packageName}.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.laoyuegou.android.mvpbase.BaseMvpFragment;
import com.laoyuegou.android.${packageName}.contract.${functionName}Contract;
import com.laoyuegou.android.${packageName}.presenter.${functionName}Presenter;

/**
* date: ${date}.
*
*/
public class ${functionName}Fragment 
        extends BaseMvpFragment<${functionName}Contract.View, ${functionName}Contract.Presenter>
        implements ${functionName}Contract.View {

    public static final String TAG = ${functionName}Fragment.class.getSimpleName();

    /**
    * ———————————————— ↓↓↓↓ BaseMvpFragment code ↓↓↓↓ ————————————————
    */
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public ${functionName}Contract.Presenter createPresenter() {
        return new ${functionName}Presenter();
    }



    /**
    * ———————————————— ↓↓↓↓ Lifecycle code ↓↓↓↓ ————————————————
    */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.cancelRequest();
    }

    /**
    * ———————————————— ↓↓↓↓ MvpView code ↓↓↓↓ ————————————————
    */

    @Override
    public void showLoading() {
        if (isAdded()) {
            getBaseActivity().showLoadingDialog(true);
        }
    }

    @Override
    public void dismissLoading() {
        if (isAdded()) {
            getBaseActivity().dismissLoadingDialog();
        }
    }

    @Override
    public void showError(String errMes) {

    }

    @Override
    public void showNull() {

    }

    @Override
    public boolean isAlived() {
        return isAdded();
    }

    /**
    * ———————————————— ↓↓↓↓ ${functionName}Fragment.View code ↓↓↓↓ ————————————————
    */

}'''

    //Presenter模板
    def mvpPresenterTemplateText = '''package com.laoyuegou.android.${packageName}.presenter;

import com.laoyuegou.android.lib.mvp.MvpBasePresenter;
import com.laoyuegou.android.${packageName}.contract.${functionName}Contract;

/**
* date: ${date}.
*
*/
public class ${functionName}Presenter extends MvpBasePresenter<${functionName}Contract.View>
    implements ${functionName}Contract.Presenter{

    @Override
    public void start() {

    }

    @Override
    public void cancelRequest() {

    }

    @Override
    public void destroyPresenter() {
        cancelRequest();
    }
}'''
    
    @TaskAction
    def generateMvpFile() {
        def mvpArray = [
            [
                templateName : mvpContractTemplateText,
                type : "contract",
                fileName : "Contract.java"
            ],
            [
                templateName : mvpPresenterTemplateText,
                type : "presenter",
                fileName : "Presenter.java"
            ], 
            [
                templateName : mvpFragmentTemplateText,
                type : "fragment",
                fileName : "Fragment.java"
            ]
        ]

        println "projectDir=" + project.projectDir
        def xibaMvpExtension = project.extensions.getByType(XibaMvpExtension)
        println "xibaMvpExtension.applicationId=" + xibaMvpExtension.applicationId


        def packageName = xibaMvpExtension.packageName
        def functionName = xibaMvpExtension.functionName

        println "packageName=" + packageName
        println "functionName=" + functionName

        def packageFilePath = xibaMvpExtension.applicationId.replace(".", "/");
        println "packageFilePath=" + packageFilePath

        def fullPath =  project.projectDir.toString() + "/src/main/java/" + packageFilePath
        println "fullPath=" + fullPath

        // Scanner scanner = new Scanner(System.in); 
        // println "请输入包名:"
        // def packageName = scanner.nextLine();

        // println "请输入类名:"
        // def functionName = scanner.nextLine();
        // println ""

        String dateString = getFormatTime();

        def mBinding = [
            packageName  : packageName,
            functionName : functionName,
            date         : dateString
        ];

        generateMvpFile(mvpArray, mBinding, fullPath)

    }

    void generateMvpFile(def mvpArray, def binding, def fullPath){
        
        for(int i = 0; i < mvpArray.size(); i++){
            preGenerateFile(mvpArray[i], binding, fullPath)
        }
    }

    void preGenerateFile(def map, def binding, def fullPath){
        // File mvpContractTemplateFile = new File("template/" + map.templateFileName)

        println "preGenerateFile : map.templateName=" + map.templateName
        println "preGenerateFile : map.type=" + map.type
        println "preGenerateFile : map.fileName=" + map.fileName
        def template = makeTemplate(map.templateName, binding);

        def path = fullPath + "/" + binding.packageName + "/" + map.type;
        def fileName = path + "/" + binding.functionName + map.fileName

        println template.toString()

        generateFile(path, fileName, template)
    }

    void generateFile(def path, def fileName, def template){
        //验证文件路径，没有则创建
        
        validatePath(path);

        File mvpFile = new File(fileName);
        
        if(!mvpFile.exists()){
            mvpFile.createNewFile()
        }

        FileOutputStream out = new FileOutputStream(mvpFile, false)
        out.write(template.toString().getBytes("utf-8"))
        out.close()
    }

    void validatePath(def path){
        File mvpFileDir = new File(path);

        if(!mvpFileDir.exists()){
            mvpFileDir.mkdirs()
        }
    }

    def getFormatTime(){
        Date date = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    /**
    * 根据模板文件生成字符串
    */
    def makeTemplate(def file, def binding){

        println "makeTemplate : file=" + file

        def engine = new groovy.text.GStringTemplateEngine();
        return engine.createTemplate(file).make(binding)
    }

    
}
