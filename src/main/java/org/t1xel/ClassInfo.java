package org.t1xel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClassInfo {
    public String name;
    public String superClass;
    public String[] interfaces;
    private String[] cpStrings;
    private short[] cpClasses;
    private static final int HEAD = -889275714;
    private static final byte CONSTANT_Utf8 = 1;
    private static final byte CONSTANT_Integer = 3;
    private static final byte CONSTANT_Float = 4;
    private static final byte CONSTANT_Long = 5;
    private static final byte CONSTANT_Double = 6;
    private static final byte CONSTANT_Class = 7;
    private static final byte CONSTANT_String = 8;
    private static final byte CONSTANT_FieldRef = 9;
    private static final byte CONSTANT_MethodRef = 10;
    private static final byte CONSTANT_InterfaceMethodRef = 11;
    private static final byte CONSTANT_NameAndType = 12;
    private static final byte CONSTANT_MethodHandle = 15;
    private static final byte CONSTANT_MethodType = 16;
    private static final byte CONSTANT_InvokeDynamic = 18;

    public ClassInfo(byte[] classData) {
        this.parse(ByteBuffer.wrap(classData));
    }

    private void parse(ByteBuffer buf) {
        if (buf.order(ByteOrder.BIG_ENDIAN).getInt() == -889275714) {
            buf.getChar();
            buf.getChar();
            int num = buf.getChar();
            this.cpStrings = new String[num];
            this.cpClasses = new short[num];

            int i;
            for(i = 1; i < num; ++i) {
                byte tag = buf.get();
                switch(tag) {
                    case 1:
                        this.cpStrings[i] = this.decodeString(buf);
                        break;
                    case 2:
                    case 13:
                    case 14:
                    case 17:
                    default:
                        return;
                    case 3:
                        buf.getInt();
                        break;
                    case 4:
                        buf.getFloat();
                        break;
                    case 5:
                        buf.getLong();
                        ++i;
                        break;
                    case 6:
                        buf.getDouble();
                        ++i;
                        break;
                    case 7:
                        this.cpClasses[i] = buf.getShort();
                        break;
                    case 8:
                    case 16:
                        buf.getChar();
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        buf.getChar();
                        buf.getChar();
                        break;
                    case 15:
                        buf.get();
                        buf.getChar();
                        break;
                    case 18:
                        buf.getChar();
                        buf.getChar();
                }
            }

            buf.getChar();
            this.name = this.cpStrings[this.cpClasses[buf.getChar()]].replace('/', '.');
            this.superClass = this.cpStrings[this.cpClasses[buf.getChar()]].replace('/', '.');
            this.interfaces = new String[buf.getChar()];

            for(i = 0; i < this.interfaces.length; ++i) {
                this.interfaces[i] = this.cpStrings[this.cpClasses[buf.getChar()]].replace('/', '.');
            }

        }
    }

    private String decodeString(ByteBuffer buf) {
        int size = buf.getChar();
        int oldLimit = buf.limit();
        buf.limit(buf.position() + size);
        StringBuilder sb = new StringBuilder(size + (size >> 1) + 16);

        while(buf.hasRemaining()) {
            byte b = buf.get();
            if (b > 0) {
                sb.append((char)b);
            } else {
                int b2 = buf.get();
                if ((b & 240) != 224) {
                    sb.append((char)((b & 31) << 6 | b2 & 63));
                } else {
                    int b3 = buf.get();
                    sb.append((char)((b & 15) << 12 | (b2 & 63) << 6 | b3 & 63));
                }
            }
        }

        buf.limit(oldLimit);
        return sb.toString();
    }
}
