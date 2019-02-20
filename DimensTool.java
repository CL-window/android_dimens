import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * 获取手机的屏幕分辨率命令(高通平台)：
 * adb shell wm size
 * --> Physical size: 1440x2880
 * 获取手机的屏幕分辨率命令(通用方法)：
 * adb shell dumpsys window displays |head -n 4
 * --> init=1080x1920 480dpi cur=1080x1920 app=1080x1920 rng=1080x1005-1920x1845
 * 
 * 首先要知道3个概念：
 * 1. 分辨率（px）：屏幕中所有物理像素点数。如1080x1920，就表示宽方向有1080个像素，高方向有1920个像素
 * 2. 密度（dp/dip）：密度无关像素。android将160dp作为一个标准（即单位面积上有160个像素点时），此时1dp=1px（最好以这个标准来做界面）；
 * 当将1dp放到240dpi的屏幕上去时，android就会自动将1dp调整为1.5px的大小。
 * 3. 屏幕像素密度(dpi)
 * 
 * sw<N>dp，如layout-sw600dp，values-sw600dp，这里的sw代表smallwidth的意思，当你所有屏幕的最小宽度都大于600dp时，
 * 屏幕就会自动到带sw600dp后缀的资源文件里去寻找相关资源文件，这里的最小宽度是指屏幕宽高的较小值，每个屏幕都是固定的，不会随着屏幕横向纵向改变而改变。
 * 
 * 对于sw<N>dp内dp的计算，google提供了计算方法： dp=（屏幕像素x160）/屏幕像素密度
 * 对于1080x1920的手机,480dpi，计算宽和高
 * 宽（dp）=（1080x160）/480=360dp
 * 高（dp）=（1920x160）/480=640dp
 * 最小宽度为432dp，可以建一个values-sw360dp的文件夹
 * 
 * 假设设计师提供的设计稿按照屏幕分辨率为1080x1920，屏幕像素密度480dpi来设计界面
 * 
 */
public class DimensTool {

    private static final int MAX_SIZE = 200; // max 800dp
    private static final String XML_DIMEN_TEMPLETE = "    <dimen name=\"common_measure_%1$ddp\">%2$.2fdp</dimen>\r\n";// xml file dimen templete

    /**
     * 这里可以任意添加
     */
    public enum DimensTypes {
        DP_300(300),
        DP_320(320),
        DP_340(340),
        DP_360(360),
        DP_380(380),
        DP_420(420),
        DP_480(480),
        DP_520(520),
        DP_600(600),
        DP_720(720),
        DP_800(800),
        DP_1080(1080),
        DP_1440(1440);

        public int width;
        DimensTypes(int width) {
            this.width = width;
        }
    }

    public static void main(String[] rags) {
        DimensTypes standard = DimensTypes.DP_360; // 设计的标准 FIXME
        DimensTool tool = new DimensTool();
        DimensTypes[] values = DimensTypes.values();

        File output = new File("output");
        tool.mkDirs(output);
        for (int i = 0; i < values.length; i++) {
            tool.makeDimens(output, values[i].width, standard.width);
        }
    }

    private void makeDimens(File outputDir, int target, int standard) {

        File childDir = new File(outputDir, "values-sw" + target + "dp");
        mkDirs(childDir);
        File targetFile = new File(childDir, "dimens.xml");
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"); // xml file head
            builder.append("<resources>\r\n"); // xml file resources start
            float result = 0.0f;
            for (int index = 0; index <= MAX_SIZE; index++) {
                result = index * 1.0f * target / standard;
                builder.append(String.format(Locale.ENGLISH, XML_DIMEN_TEMPLETE, index, result));
            }

            builder.append("</resources>\r\n"); // xml file resources end
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(builder.toString().getBytes());
            System.out.print("makeDimens " + target + " success");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void mkDirs(File target) {
        if(target.exists()) {
            deleteDir(target);
        }
        if(!target.mkdirs()) {
            System.err.print("mkdir " + target + " dir fail");
        }
    }

    /**
     * 删除文件夹
     */
    private boolean deleteDir(File dir) {
        return deleteDir(dir, 0);
    }

    private boolean deleteDir(File dir, int index) {
        if(dir.isDirectory()) {
            if(index > 100) {
                // 超过 100 层递归，防止出现死递归, 就直接返回 false
                return false;
            }

            String[] files = dir.list();
            if(files == null) {
                return false;
            }
            for (String child : files) {
                boolean success = deleteDir(new File(dir, child), index+1);
                if (!success) {
                    return false;
                }
            }
        } else if(!dir.exists()) {
            return true;
        }

        try {
            return dir.delete();
        } catch (Exception e) {
            return false;
        }
    }
}
