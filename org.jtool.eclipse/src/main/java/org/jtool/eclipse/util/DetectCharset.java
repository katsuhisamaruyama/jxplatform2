/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.util;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Detects the charset name of a Japanese text.
 * 
 * @author Katsuhisa Maruyama
 */
public class DetectCharset {
    
    enum Charset { NO_MATCH, ASCII, UTF8, JIS, SJIS, EUC, EUC_OR_SJIS }
    
    public static String getCharsetName(String filename) throws IOException {
        return getCharsetName(filename, null);
    }
    
    public static String getCharsetName(byte[] contents) throws IOException {
        return getCharsetName(null, contents);
    }
    
    private static String getCharsetName(String filename, byte[] bytes) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(createInputStream(filename, bytes));
            Charset charset = checkJisOrUnicode(bis);
            bis.close();
            
            if (charset == Charset.ASCII) {
                return "US-ASCII";
            } else if (charset == Charset.UTF8) {
                return "UTF-8";
            } else if (charset == Charset.JIS) {
                return "ISO-2022-JP";
            }
            
            bis = new BufferedInputStream(createInputStream(filename, bytes));
            charset = checkEucOrShiftJis(bis);
            if (charset == Charset.EUC) {
                return "EUC-JP";
            } else if (charset == Charset.SJIS) {
                return "SJIS";
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    System.err.println("File close error");
                }
            } 
        }
        return getDefaultCharsetName();
    }
    
    private static InputStream createInputStream(String filename, byte[] contents) throws IOException {
        if (filename != null) {
            return new FileInputStream(filename);
        }
        return new ByteArrayInputStream(contents);
    }
    
    private static String getDefaultCharsetName() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return "SJIS";
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            return "UTF-8";
        }
        return "EUC-JP";
    }
    
    private static Charset checkJisOrUnicode(InputStream is) throws IOException {
        int ch;
        while ((ch = is.read()) != -1) {
            if (ch == 0x1b) {  /* ESC (1B) */
                return checkJis(is);
            } else if (ch < 0x80) {
                /* ASCII or control sequence */
            } else if (ch < 0xc0) {
                return Charset.NO_MATCH;
            } else {
                return checkUnicode(ch, is);
            }
        }
        return Charset.ASCII;
    }
    
    private static Charset checkJis(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if (ch == 0x24) {
            if ((ch = is.read()) == -1) {
                return Charset.NO_MATCH;
            }
            if (ch == 0x42 || ch == 0x40) {
                if ((ch = is.read()) == -1) {
                    return Charset.NO_MATCH;
                }
                if (ch >= 0x21 || ch <= 0x7e) {
                    if ((ch = is.read()) == -1) {
                        return Charset.NO_MATCH;
                    }
                    if (ch >= 0x21 || ch <= 0x7e) {
                        return Charset.JIS;
                    }
                }
            }
        } else if (ch == 0x28) {
            if ((ch = is.read()) == -1) {
                return Charset.JIS;
            }
            if (ch == 0x4a || ch == 0x42) {
                if ((ch = is.read()) == -1) {
                    return Charset.NO_MATCH;
                }
                if (ch >= 0x20 || ch <= 0x7e) {
                    return Charset.JIS;
                }
            } else if (ch == 0x49) {
                if ((ch = is.read()) == -1) {
                    return Charset.NO_MATCH;
                }
                if (ch >= 0x21 && ch <= 0x5f) {
                    return Charset.JIS;
                }
            }
        }
        return Charset.NO_MATCH;
    }
    
    private static Charset checkUnicode(int ch, InputStream is) throws IOException {
        int len;
        if (ch < 0xe0) {
            len = 1;
        } else if (ch < 0xf0) {
            len = 2;
        } else if (ch < 0xf8) {
            len = 3;
        } else if (ch < 0xfc) {
            len = 4;
        } else if (ch < 0xfe) {
            len = 5;
        } else {
            return Charset.NO_MATCH;
        }
        
        for (int i = 0; i < len; i++) {
            if ((ch = is.read()) == -1) {
                return Charset.NO_MATCH;
            }
            if (ch < 0x80 || ch > 0xbf) {
                return Charset.NO_MATCH;
            }
        }
        return Charset.UTF8;
    }
    
    private static Charset checkEucOrShiftJis(InputStream is) throws IOException {
        int ch;
        Charset result = Charset.EUC_OR_SJIS;
        while ((ch = is.read()) != -1) {
            if (isShiftJis(ch)) {
                result = checkShiftJis(is);
            } else if (isEuc(ch)) {
                result = checkEuc(is);
            } else if (ch == 0x8e) {
                result = checkShiftJisOrEUCKana(is);
            } else if (ch >= 0xa1 && ch <= 0xdf) {
                result = checkShiftJisOrEUCKanji(is);
            } else if (ch >= 0xe0 && ch <= 0xef) {
                result = checkShiftJisOrEUCKanji2(is);
            }
            if (result != Charset.EUC_OR_SJIS) {
                break;
            }
        }
        return result;
    }
    
    private static boolean isShiftJis(int ch) {
        return (ch >= 0x81 && ch <= 0x8d) || (ch >= 0x8f && ch <= 0x9f);
    }
    
    private static boolean isEuc(int ch) {
        return ch >= 0xf0 && ch <= 0xfe;
    }
    
    private static Charset checkShiftJis(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if ((ch >= 0x40 && ch <= 0x7e) || (ch >= 0x80 && ch <= 0xfc)) {
            return Charset.SJIS;
        }
        return Charset.NO_MATCH;
    }
    
    private static Charset checkEuc(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if (ch >= 0xa1 && ch <= 0xfe) {
            return Charset.EUC;
        }
        return Charset.NO_MATCH;
    }
    
    private static Charset checkShiftJisOrEUCKana(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if ((ch >= 0x40 && ch <= 0x7e) || (ch >= 0x80 && ch <= 0xa0) || (ch >= 0xe0 && ch <= 0xfc)) {
            return Charset.SJIS;
        } else if (ch >= 0xa1 && ch <= 0xdf) {
            return Charset.EUC_OR_SJIS;
        }
        return Charset.NO_MATCH;
    }
    
    private static Charset checkShiftJisOrEUCKanji(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if (ch >= 0x81 && ch <= 0x9f) {
            return Charset.SJIS;
        } else if (ch >= 0xa1 && ch <= 0xdf) {
            return Charset.EUC_OR_SJIS;
        } else if (ch >= 0xe0 && ch <= 0xef) {
            if ((ch = is.read()) == -1) {
                return Charset.NO_MATCH;
            }
            while (ch >= 0x40) {
                if (isShiftJis(ch)) {
                    return Charset.SJIS;
                } else if (isEuc(ch)) {
                    return Charset.EUC;
                }
                if ((ch = is.read()) == -1) {
                    return Charset.NO_MATCH;
                }
            }
        } else if (ch >= 0xf0 && ch <= 0xfe) {
            return Charset.EUC;
        }
        return Charset.NO_MATCH;
    }
    
    private static Charset checkShiftJisOrEUCKanji2(InputStream is) throws IOException {
        int ch;
        if ((ch = is.read()) == -1) {
            return Charset.NO_MATCH;
        }
        if ((ch >= 0x40 && ch <= 0x7e) || (ch >= 0x80 && ch <= 0xa0)) {
            return Charset.SJIS;
        } else if (ch >= 0xe0 && ch <= 0xfc) {
            return Charset.EUC_OR_SJIS;
        } else if (ch >= 0xfd && ch >= 0xfe){
            return Charset.EUC;
        }
        return Charset.NO_MATCH;
    }
}
