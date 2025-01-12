import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Ex2Tests {
    @Test
    void testIsForm(){
        SCell cell = new SCell("Hey", new Ex2Sheet());
        String[] validFormulas = {
                "=1", "=1+2", "=(1+2)", "=2*3", "=(2*3)", "=(1+2)*3", "=A1", "=A2+3", "=(1+2)*((3))-1",
                "=(2+A3)/A2", "=(A1*2)", "=A1+A2*B3", "=C1/D2", "=(A1+2)*(B3-C4)", "=(2)", "=0.5", "=0+1",
                "=(1+2)/3", "=10-3", "=1.2+0.8", "=-1", "=-(A1+B2)", "=1*2+3", "=((1*2)+3)", "=2/3", "=4/2",
                "=4/2+1", "=A2-1", "=B3+B4", "=2*A1", "=(B2+1)/C3", "=(A1)+(A2)", "=(2)+(3*2)", "=1+2+3",
                "=1+2+3+4", "=(1*2)*3", "=1*2*3*4", "=(1+2)*2/2", "=(1/2)*(2/3)", "=1.2+3.4", "=1.2-0.4",
                "=1.2*2.0", "=2.2/2.0", "=2.0*(2.0/2.0)", "=((1.2*2)/2)", "=(2*A1)", "=1+((2*3)-1)", "=1+2-3",
                "=1*2+3-4", "=1+2*3-4/5", "=0+1+2", "=1-2-3", "=1*2*3", "=1/2/3", "=(A1)+(B2)", "=1+(2/3)",
                "=A1*(2/3)", "=1*2+3*4", "=(1+2)*(3+4)", "=(1+(2*3))", "=1/2+3*4", "=(1/2)+(3/4)", "=((1/2)+(3/4))",
                "=1.5+2.5-1.0", "=1.1*2.2/2.0", "=1.2/2.0+3.4", "=(1.2*2)+(2.2/2.0)", "=A1+(B2*C3)", "=(A1)+(B2+C3)",
                "=1.2+(A1)", "=1+(A1*B2)", "=A1*A2/B3", "=(1+A1)", "=1+A1", "=(1*A1)", "=A1+(2/3)", "=1+(A2)",
                "=2*(A1+A2)", "=(A1)*(B2)", "=(1+A1)*2", "=1+2+3+(A1)", "=A1+B2*(C3)", "=A1+A2+A3", "=B1*B2*B3",
                "=(1)+(2*3)", "=(1*A1)+(2*B2)", "=1+(A1/A2)", "=A1+A2-A3", "=(A1/A2)*B3", "=(1.2)+(2.2)", "=1+(2*A1)",
                "=1/(A1+A2)", "=A1/A2+(2/3)", "=A1*A2/B3", "=A1/(2*A2)", "=1*(A1/A2)", "=(1/2)*(A1/A2)",
        };
        for (String form : validFormulas) {
            assertTrue(cell.isForm(form));
        }
        String[] invalidFormulas = {
                "=a", "=AB", "=@2", "=2+)", "=(3+1*2)-", "==1", "=1+", "=A1+2+3)", "=(1+A2", "=A1+(2-3",
                "=1*2+", "=(1/2", "=1/(2", "=1*2/3+)", "=1+2-3/", "=(1+2)*3-)", "=1-2+", "=(A1+A2-", "=1+2+3)+4",
                "=(1*A2", "=A1/(A2", "=A1-(A2", "=A1+(2", "=1+", "=1/2/", "=(1/(2)", "=1*A2/", "=1+A2-","=a100","=5+a400+2",
                "=(1+2)-(3+", "=1/(A2", "=1+(A2", "=1-(A2", "=1+(2+A2", "=1+A2/(A3", "=(A1)+(B2)-", "=A1-(B2", "=(1+A2",
                "=(1+A2-", "=(1+2)*(3)+)", "=1-2/3-", "=1/(2+3-", "=1+(A2/(B3", "=1-2/(3", "=1*2/3+", "=(1/A2", "=1-(A2",
                "=1+(A2-", "=1+(A2)+", "=1+(A2*(B3", "=1/A2*(B3", "=(A1)/(B2-", "=A1/(B2+(C3", "=(1+A2-(3+", "=(A1+A2-(3+",
                "=(1+2)-(3+", "=1+A2+", "=1+A2-", "=(A1-(B2", "=1+(A2-(B3", "=(1-(A2)-(B3", "=1/A2+(2/3-", "=1/(A2+(B3",
                "=A1/A2*(B3-", "=1+A2-", "=A1+A2+(3+", "=(A1-(A2)+(3+", "=A1-(A2)+)", "=1+A2/(B3-", "=(1+(A2+(B3-", "=1+(2*(A2)",
                "=1+(A2-(B3+", "=1+(2*(A2)-(B3+", "=1+(A2)+3*", "=1+(A2/(B3+(C4", "=(A1)-(A2)+(3+", "=(1/(A2)+(B3-", "=A1/A2/(B3-",
                "=1-(A2+(B3-", "=1/(A2+(B3)+)", "=1+(A2-(B3-(C4", "=A1+A2+(B3+(C4-", "=1/(A2)+3*(B3", "=1/(A2)-(B3+(C4", "=A1-(A2+(B3-",
                "=(1/(A2)+(B3)-(C4", "=1/(A2)+(3/A3", "=1/A2/(B3-(C4"
        };
        for (String form : invalidFormulas) {
            assertFalse(cell.isForm(form));
        }
    }

    @Test
    void testISIn() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        int x = 8,y=15;
        assertTrue(sheet.isIn(x, y));
        x = -1;
        y = 5;
        assertFalse(sheet.isIn(x, y));
        x = 15;
        y = -7;
        assertFalse(sheet.isIn(x, y));
        x = 25;
        y = 100;
        assertFalse(sheet.isIn(x, y));
    }

    @Test
    void testValue()throws Exception{
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0,0,"=15+2");
        sheet.eval();
        assertEquals("17.0",sheet.value(0,0));
        sheet.set(0,0,"=15G+2");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM,sheet.value(0,0));
        sheet.set(0,0,"=18/9*3");
        sheet.eval();
        assertEquals("6.0",sheet.value(0,0));
    }

    @Test
    void testCompute() throws Exception {
        String s1 = "((4+2)*2/4)+35";
        SCell cell = new SCell(s1, new Ex2Sheet());
        assertEquals(38,cell.computeForm(cell.getData()));
        cell.setData("(((4+2)*2/4)+35)/2");
        assertEquals(19,cell.computeForm(cell.getData()));
        cell.setData("=(50-30)/(2+4)");
        assertEquals(3.3333333333333335,cell.computeForm(cell.getData()));
        cell.setData("=(8+(3*(4+2)))-(7/(5-2))");
        assertEquals(23.666666666666668,cell.computeForm(cell.getData()));
    }



    @Test
    void testIsVaild() throws Exception {
        CellEntry entry = new CellEntry("A40");
        assertTrue(entry.isValid());
        entry.setIndex("Z30");
        assertTrue(entry.isValid());
        entry.setIndex("G400");
        assertFalse(entry.isValid());
        entry.setIndex("B");
        assertFalse(entry.isValid());
        entry.setIndex("$15");
        assertFalse(entry.isValid());
        entry.setIndex("A##");
        assertFalse(entry.isValid());
        entry.setIndex("B-1");
        assertFalse(entry.isValid());
    }
    @Test
    void testGetXY()throws Exception {
        CellEntry entry = new CellEntry("A40");
        assertEquals(0,entry.getX());
        assertEquals(40,entry.getY());
        entry.setIndex("Z30");
        assertEquals(25,entry.getX());
        assertEquals(30,entry.getY());
        entry.setIndex("G15");
        assertEquals(6,entry.getX());
        assertEquals(15,entry.getY());
    }


}