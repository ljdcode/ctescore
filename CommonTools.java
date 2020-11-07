package bjut.ai.bn;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CommonTools
{
	public static ArrayList getFiles(String dir, ArrayList files) {
		File file = new File(dir);
		File[] filearray = file.listFiles();
		for(int i = 0; i < filearray.length; i++)
		{
			if(filearray[i].isDirectory())
			{
				getFiles(filearray[i].getAbsolutePath(), files);
			}else
			{
				String filepath = filearray[i].getAbsolutePath();
				files.add(filepath);
			}
		}

		return files;
		
	}
	public static ArrayList getFiles(String dir, ArrayList files, String posfix) {
		File file = new File(dir);
		File[] filearray = file.listFiles();
		for (int i = 0; i < filearray.length; i++) {
			if (filearray[i].isDirectory()) {
				getFiles(filearray[i].getAbsolutePath(), files, posfix);
			} else if (filearray[i].getName().endsWith(posfix)) {
				String filepath = filearray[i].getAbsolutePath();
				files.add(filepath);
			}
		}

		return files;

	}
	
    public static void outArray(int[] a, PrintWriter pw)
    {
        for(int ix = 0; ix < a.length; ix++)
            pw.append(a[ix]+",");
        pw.flush();
    }
    public static void outArray(String[] a, PrintWriter pw)
    {
        for(int ix = 0; ix < a.length; ix++)
            pw.append(a[ix]+",");
        pw.flush();
    }

    public static void outArray(Object[][] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     for (int j = 0; j < o[0].length; j++)
     {
       System.out.print(o[i][j] + ",");
     }
     System.out.println();
   }
 }

 public static void outArray(Object[] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     System.out.print("\"" + o[i] + "\"" + ",");
   }
   System.out.println();

 }
 public static void outArray(boolean[] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     System.out.print(o[i] + ",");
   }
   System.out.println();
 }

 public static void outArray(char[] o)
  {
    for (int i = 0; i < o.length; i++)
    {
      System.out.print(o[i] + ",");
    }
    System.out.println();
 }

 public static void outArray(int[][] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     for (int j = 0; j < o[0].length; j++)
     {
       System.out.print(o[i][j] + ",");
     }
     System.out.println();
   }
 }

 public static void outArray(double[][] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     for (int j = 0; j < o[0].length; j++)
     {
       System.out.print(o[i][j] + ",");
     }
     System.out.println();
   }
 }

 public static void outArray(int[] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     System.out.print(o[i] + ",");
   }
   System.out.println();
 }

 public static void outArray(double[] o)
 {
   for (int i = 0; i < o.length; i++)
   {
     System.out.print(o[i] + ",");
   }
   System.out.println();

 }

    public static PrintWriter getPrintWriter(String path, String filename)
    {
        PrintWriter pw = null;
        try
        {
            File filepath = new File(path);
            filepath.mkdirs();
            File file = new File(filepath,filename);
            pw = new PrintWriter(new FileWriter(file, false),true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return pw;
    }
    
    public static PrintWriter getPrintWriter(String fullpath) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(fullpath, false), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pw;
	}
    public static PrintWriter getPrinWriter(String filename) throws IOException
   {
       File dir = new File("c:" + File.separator + "BayesianLog_Alarm");
       dir.mkdir();
       File file = new File(dir, filename+".csv");

       return new PrintWriter(new FileWriter(file, false));
   }
   public static void arraycopy(Object[][] src, Object[][] des)
   {
       for(int i = 0; i < src.length; i++)
       {
           System.arraycopy(src[i],0,des[i],0,src[i].length);
       }
   }
   public static void arraycopy(double[][] src, double[][] des)
   {
       for(int i = 0; i < src.length; i++)
       {
           System.arraycopy(src[i],0,des[i],0,src[i].length);
       }
   }
   public static void arraycopy(String[][] src, double[][] des)
   {
       for (int i = 0; i < src.length; i++)
       {
           System.arraycopy(src[i], 0, des[i], 0, src[i].length);
       }
   }

   public static void arraycopy(int[][] src, int[][] des)
{
    for(int i = 0; i < src.length; i++)
    {
        System.arraycopy(src[i],0,des[i],0,src[i].length);
    }
}



   public static void main(String[] args)
   {
       try
       {
          String dir = "c:\\TBN";
			ArrayList files = new ArrayList<String>();
			CommonTools.getFiles(dir, files);
			System.out.println(files);

       }
       catch (Exception ex)
       {
           ex.printStackTrace();
       }

    }
}
