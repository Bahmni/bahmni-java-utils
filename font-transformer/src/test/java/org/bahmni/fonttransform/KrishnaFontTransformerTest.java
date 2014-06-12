package org.bahmni.fonttransform;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KrishnaFontTransformerTest {

    KrishnaFontTransformer transformer = null;

    @Before
    public void init(){
        transformer = new KrishnaFontTransformer();
    }

    @Test
    public void shouldTransformFromKrishnaToUnicode() {
        assertEquals("दर्शन", transformer.krishnaToUnicode("n'kZu"));
        assertEquals("निकुरे", transformer.krishnaToUnicode("fudqjs"));
        assertEquals("वाल्मीक", transformer.krishnaToUnicode("okYehd"));
        assertEquals("पुरूषोत्तम", transformer.krishnaToUnicode("iq:\"kksRre"));
        assertEquals("सुधाकर", transformer.krishnaToUnicode("lq/kkdj"));
        assertEquals("केव्ळराम", transformer.krishnaToUnicode("dsOGjke"));
    }

    @Test
    public void shouldTransformKrishnaEToCorrectHindiE() {
        assertEquals("जनाबाई", transformer.krishnaToUnicode("tukckbZ"));
        assertEquals("ई", transformer.krishnaToUnicode("bZ"));
        assertEquals("इ", transformer.krishnaToUnicode("b"));

    }

    @Test
    public void shouldPlaceSmallEMatraOneForward() {
        assertEquals("रविंद्र", transformer.krishnaToUnicode("jfoanz"));
        assertEquals("रवि", transformer.krishnaToUnicode("jfo"));
    }

    @Test
    public void shouldPlaceUpROneBack() {
        assertEquals("अार्यन", transformer.krishnaToUnicode("vk;Zu"));
        assertEquals("बर्फी", transformer.krishnaToUnicode("cQhZ"));
        assertEquals("बर्फि", transformer.krishnaToUnicode("cQfZ"));
    }

    @Test
    public void shouldPlaceUpRTwoBackIfPrecededByAey() {
        assertEquals("दुर्गा", transformer.krishnaToUnicode("nqxkZ"));
        assertEquals("वर्षा", transformer.krishnaToUnicode("o\"kkZ"));
    }


    @Test
    public void shouldReplaceHalfConsonantsFollowedByKWithFullConsonants() {
        assertEquals("मंडल", transformer.krishnaToUnicode("eaMYk"));
        assertEquals("निवृता", transformer.krishnaToUnicode("fuo`Rkk"));
        assertEquals("नन्नावारे", transformer.krishnaToUnicode("uUUkkokjs"));

        assertEquals("चुधरी", transformer.krishnaToUnicode("pq/kjh"));
        assertEquals("माधुरी", transformer.krishnaToUnicode("ek/kqjh"));
        assertEquals("प्रभाकर", transformer.krishnaToUnicode("izHkkdj"));
        assertEquals("प्रतिक्षा", transformer.krishnaToUnicode("izfr{kk"));
        assertEquals("भेद्रुजी", transformer.krishnaToUnicode("Hksnzqth"));
        assertEquals("शंकरजी", transformer.krishnaToUnicode("'kadjth"));
        assertEquals("प्रीती", transformer.krishnaToUnicode("izhrh"));
    }

    @Test
    public void shouldReplaceSmallMatraEFollowedByHalfConsonants() {
        assertEquals("भिवाजी", transformer.krishnaToUnicode("fHkokth"));
    }

    @Test
    public void shouldReplaceAiMatraCorrectly() {
        assertEquals("कैलाश", transformer.krishnaToUnicode("dSyk'k"));
        assertEquals("वैशाली", transformer.krishnaToUnicode("oS'kkyh"));
    }

    @Test
    public void shouldTransformOMatra() {
        assertEquals("शोधग्राम", transformer.krishnaToUnicode("'kks/kxzke"));
        assertEquals("घोडेझरी", transformer.krishnaToUnicode("?kksMs>jh"));
    }
}
