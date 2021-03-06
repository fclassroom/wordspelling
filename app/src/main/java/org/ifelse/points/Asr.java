package org.ifelse.points;

import com.alibaba.fastjson.JSON;
import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import org.ifelse.models.MAsr;
import org.ifelse.wordspelling.JKApp;
import org.ifelse.vl.FlowBox;
import org.ifelse.vl.FlowPoint;
import org.ifelse.vl.NLog;

public class Asr extends FlowPoint {
    @Override
    public void run(final FlowBox flowBox) throws Exception {


        //flowBox.setValue(params.get("result"),result);



        new Thread(){


            @Override
            public void run() {


                JKApp.instance.asr.registerListener(new EventListener() {


                    String result;
                    @Override
                    public void onEvent(String name, String params, byte[] bytes, int offset, int length) {

                        flowBox.log("asr name:%s params:%s ",name,params);
                        switch (name){

                            case SpeechConstant.CALLBACK_EVENT_ASR_END:

                                JKApp.instance.asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
                                break;
                            case SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL:
                            {
                                    result = params;
                                    flowBox.log(result);
                            }
                                break;
                            case  SpeechConstant.CALLBACK_EVENT_ASR_EXIT: {
                                JKApp.instance.asr.unregisterListener(this);

                                if( result != null ) {

                                    try {

                                        flowBox.log("ASR result str:%s",result);
                                        MAsr mAsr = JSON.parseObject(result, MAsr.class);
                                        if (mAsr.getError() == 0) {
                                            String str = mAsr.getBest_result();

                                            flowBox.log("ASR result ok:%s",str);

                                            setValue(flowBox, "result", str.replace(" ", ""));
                                        }

                                    } catch (Exception e) {


                                        NLog.e(e);
                                    }
                                }



                                flowBox.next();
                            }
                                break;


                        }


                    }
                });

                //String json ="{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":false,\"pid\":1737}";
                String json ="{\"accept-audio-volume\":false}";


                JKApp.instance.asr.send(SpeechConstant.ASR_START, json, null, 0, 0);




            }
        }.start();


        Thread.sleep(10000);







    }
}
