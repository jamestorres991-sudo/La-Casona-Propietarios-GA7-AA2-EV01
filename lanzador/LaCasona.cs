using System;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;

// Abre el mÃ³dulo desde La Casona.exe, sin escribir comandos.
internal static class LaCasona {
    [STAThread]
    private static void Main() {
        string carpeta = AppDomain.CurrentDomain.BaseDirectory;
        try {
            string java = BuscarJava();
            if (java == null) {
                MessageBox.Show("No se encontrÃ³ Java. Se necesita un JDK 17 o superior. " +
                    "Si tienes Android Studio, comprueba que su instalaciÃ³n estÃ© completa.",
                    "La Casona", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }
            string jar = Path.Combine(carpeta, "dist", "propietarios.jar");
            string conector = Path.Combine(carpeta, "lib", "mysql-connector-j-9.7.0.jar");
            if (!File.Exists(jar) || !File.Exists(conector)) {
                MessageBox.Show("Extrae toda la carpeta del ZIP antes de abrir La Casona.exe. " +
                    "El ejecutable necesita las carpetas dist, lib y sql que lo acompaÃ±an.",
                    "La Casona");
                return;
            }
            ProcessStartInfo inicio = new ProcessStartInfo();
            inicio.FileName = java;
            inicio.WorkingDirectory = carpeta;
            inicio.Arguments = "-cp \"" + jar + ";" + conector +
                "\" lacasona.herramientas.InicioAutomatico \"" + carpeta.TrimEnd('\\') + "\"";
            inicio.UseShellExecute = false;
            inicio.CreateNoWindow = true;
            Process.Start(inicio).WaitForExit();
        } catch (Exception error) {
            MessageBox.Show("No se pudo abrir la aplicaciÃ³n. " + error.Message, "La Casona");
        }
    }

    private static string BuscarJava() {
        string variable = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!String.IsNullOrEmpty(variable)) {
            string archivo = Path.Combine(variable, "bin", "javaw.exe");
            if (File.Exists(archivo)) { return archivo; }
        }
        string programas = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        string android = Path.Combine(programas, "Android", "Android Studio", "jbr", "bin", "javaw.exe");
        if (File.Exists(android)) { return android; }
        foreach (string nombre in new string[] {"Java", "Eclipse Adoptium", "Microsoft"}) {
            string raiz = Path.Combine(programas, nombre);
            if (!Directory.Exists(raiz)) { continue; }
            foreach (string ruta in Directory.GetDirectories(raiz)) {
                string archivo = Path.Combine(ruta, "bin", "javaw.exe");
                if (File.Exists(archivo)) { return archivo; }
            }
        }
        return null;
    }
}
