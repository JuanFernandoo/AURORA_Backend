import java.lang.reflect.*;

public class MedicionLatencia {
    public static void main(String[] a) throws Exception {
        Class<?> cfg = Class.forName("com.aurora.config.ConfiguracionAurora");
        Object c = cfg.getMethod("getInstance").invoke(null);
        Class<?> s = Class.forName("com.aurora.api.SimulacionApiService");
        Constructor<?> k = s.getDeclaredConstructors()[0];
        k.setAccessible(true);
        Object svc = k.newInstance(c);
        Class<?> t = Class.forName("com.aurora.factory.TipoControlador");
        Method m = s.getDeclaredMethod("ejecutar", t, int.class);
        m.setAccessible(true);
        Object ad = Enum.valueOf((Class) t, "ADAPTATIVO");
        for (int i = 0; i < 2000; i++)
            m.invoke(svc, ad, 15);
        int n = 5000;
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            long t0 = System.nanoTime();
            m.invoke(svc, ad, 15);
            d[i] = (System.nanoTime() - t0) / 1e6 / 15.0;
        }
        java.util.Arrays.sort(d);
        double sum = 0;
        for (double x : d)
            sum += x;
        System.out.printf("n=%d media=%.5f ms p95=%.5f ms max=%.5f ms por decision%n", n, sum / n, d[(int) (n * .95)],
                d[n - 1]);
    }
}
