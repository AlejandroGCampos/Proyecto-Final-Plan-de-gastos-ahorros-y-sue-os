package com.example.expoporgra2prompt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * DBHelper gestiona la base de datos local SQLite para gastos, ahorros y metas.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finanzas.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creación de tablas
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Usuario (usuario TEXT PRIMARY KEY, password TEXT)");
        db.execSQL("CREATE TABLE Gasto (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT, monto REAL, descripcion TEXT, categoria TEXT, fecha TEXT)");
        db.execSQL("CREATE TABLE Ahorro (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT, monto REAL, descripcion TEXT, fecha TEXT)");
        db.execSQL("CREATE TABLE Meta (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT, nombre TEXT, monto_objetivo REAL, monto_acumulado REAL, descripcion TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Usuario");
        db.execSQL("DROP TABLE IF EXISTS Gasto");
        db.execSQL("DROP TABLE IF EXISTS Ahorro");
        db.execSQL("DROP TABLE IF EXISTS Meta");
        onCreate(db);
    }

    // Registro de usuario robusto
    public boolean registrarUsuario(String usuario, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getWritableDatabase();
            cursor = db.rawQuery("SELECT usuario FROM Usuario WHERE usuario = ?", new String[]{usuario});
            boolean existe = cursor.moveToFirst();
            cursor.close();
            if (existe) {
                db.close();
                return false; // Usuario ya existe
            }
            ContentValues values = new ContentValues();
            values.put("usuario", usuario);
            values.put("password", password);
            long result = db.insert("Usuario", null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            return false;
        }
    }

    // Validación de usuario robusta
    public boolean validarUsuario(String usuario, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT usuario FROM Usuario WHERE usuario = ? AND password = ?", new String[]{usuario, password});
            boolean valido = cursor.moveToFirst();
            cursor.close();
            db.close();
            return valido;
        } catch (Exception e) {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            return false;
        }
    }

    // Verifica si el usuario existe
    public boolean existeUsuario(String usuario) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT usuario FROM Usuario WHERE usuario = ?", new String[]{usuario});
            boolean existe = cursor.moveToFirst();
            cursor.close();
            db.close();
            return existe;
        } catch (Exception e) {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            return false;
        }
    }

    // Inserta un gasto vinculado al usuario
    public boolean insertarGasto(String usuario, double monto, String descripcion, String categoria, String fecha) {
        if (usuario.isEmpty() || monto <= 0 || descripcion.isEmpty() || categoria.isEmpty() || fecha.isEmpty()) return false;
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("monto", monto);
        values.put("descripcion", descripcion);
        values.put("categoria", categoria);
        values.put("fecha", fecha);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert("Gasto", null, values);
        db.close();
        return result != -1;
    }

    // Consulta los gastos de un usuario
    public Cursor obtenerGastos(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Gasto WHERE usuario = ? ORDER BY fecha DESC", new String[]{usuario});
    }

    // Inserta un ahorro vinculado al usuario
    public boolean insertarAhorro(String usuario, double monto, String descripcion, String fecha) {
        if (usuario.isEmpty() || monto <= 0 || descripcion.isEmpty() || fecha.isEmpty()) return false;
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("monto", monto);
        values.put("descripcion", descripcion);
        values.put("fecha", fecha);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert("Ahorro", null, values);
        db.close();
        return result != -1;
    }

    // Consulta los ahorros de un usuario
    public Cursor obtenerAhorros(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Ahorro WHERE usuario = ? ORDER BY fecha DESC", new String[]{usuario});
    }

    // Inserta una meta vinculada al usuario
    public boolean insertarMeta(String usuario, String nombre, double monto_objetivo, String descripcion) {
        if (usuario.isEmpty() || nombre.isEmpty() || monto_objetivo <= 0 || descripcion.isEmpty()) return false;
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("nombre", nombre);
        values.put("monto_objetivo", monto_objetivo);
        values.put("monto_acumulado", 0);
        values.put("descripcion", descripcion);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert("Meta", null, values);
        db.close();
        return result != -1;
    }

    // Consulta las metas de un usuario
    public Cursor obtenerMetas(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Meta WHERE usuario = ?", new String[]{usuario});
    }

    // Obtiene el balance general del usuario
    public double obtenerBalanceGeneral(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cAhorro = db.rawQuery("SELECT SUM(monto) FROM Ahorro WHERE usuario = ?", new String[]{usuario});
        Cursor cGasto = db.rawQuery("SELECT SUM(monto) FROM Gasto WHERE usuario = ?", new String[]{usuario});
        double totalAhorro = 0, totalGasto = 0;
        if (cAhorro.moveToFirst() && !cAhorro.isNull(0)) totalAhorro = cAhorro.getDouble(0);
        if (cGasto.moveToFirst() && !cGasto.isNull(0)) totalGasto = cGasto.getDouble(0);
        cAhorro.close();
        cGasto.close();
        db.close();
        return totalAhorro - totalGasto;
    }

    // Obtiene el balance general (global, sin filtrar por usuario)
    public double obtenerBalanceGeneral() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cAhorro = db.rawQuery("SELECT SUM(monto) FROM Ahorro", null);
        Cursor cGasto = db.rawQuery("SELECT SUM(monto) FROM Gasto", null);
        double totalAhorro = 0, totalGasto = 0;
        if (cAhorro.moveToFirst() && !cAhorro.isNull(0)) totalAhorro = cAhorro.getDouble(0);
        if (cGasto.moveToFirst() && !cGasto.isNull(0)) totalGasto = cGasto.getDouble(0);
        cAhorro.close();
        cGasto.close();
        db.close();
        return totalAhorro - totalGasto;
    }

    // Consejo financiero: advertir si gasta mucho en una categoría
    public String advertirGastoCategoria(String categoria, double limite) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Gasto WHERE categoria = ?", new String[]{categoria});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        if (total > limite) {
            return "¡Cuidado! Estás gastando mucho en " + categoria;
        }
        return "Gasto en " + categoria + " dentro de lo normal.";
    }

    // Obtiene el total de gastos (para un usuario)
    public double getTotalGastos(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Gasto WHERE usuario = ?", new String[]{usuario});
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Mantener compatibilidad: total sin usuario
    public double getTotalGastos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Gasto", null);
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Obtiene el total de ahorros (para un usuario)
    public double getTotalAhorros(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Ahorro WHERE usuario = ?", new String[]{usuario});
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Mantener compatibilidad: total de ahorros sin usuario
    public double getTotalAhorros() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Ahorro", null);
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Advertir gasto por categoria para un usuario
    public String advertirGastoCategoriaUsuario(String usuario, String categoria, double limite) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(monto) FROM Gasto WHERE usuario = ? AND categoria = ?", new String[]{usuario, categoria});
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        if (total > limite) {
            return "¡Cuidado! Estás gastando mucho en " + categoria;
        }
        return "Gasto en " + categoria + " dentro de lo normal.";
    }

    // Agrega ahorro a una meta existente
    public boolean agregarAhorroAMeta(int idMeta, double monto) {
        if (monto <= 0) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT monto_acumulado FROM Meta WHERE id = ?", new String[]{String.valueOf(idMeta)});
        double acumulado = 0;
        if (cursor.moveToFirst()) {
            acumulado = cursor.getDouble(0);
        }
        cursor.close();
        double nuevoAcumulado = acumulado + monto;
        ContentValues values = new ContentValues();
        values.put("monto_acumulado", nuevoAcumulado);
        int rows = db.update("Meta", values, "id = ?", new String[]{String.valueOf(idMeta)});
        db.close();
        return rows > 0;
    }

    public double obtenerTotalGastos(String usuario) {
        // TODO: Implementar lógica real
        return 0;
    }
    public double obtenerTotalAhorros(String usuario) {
        // TODO: Implementar lógica real
        return 0;
    }
    public String obtenerResumenMetas(String usuario) {
        // TODO: Implementar lógica real
        return "";
    }
}
