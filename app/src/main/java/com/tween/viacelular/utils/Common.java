package com.tween.viacelular.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * Constantes comunes de uso dentro de la app
 * Created by davidfigueroa on 19/10/15.
 */
public class Common
{
	public static DisplayImageOptions	DEFAULT_OPTIONS				= null; //Opciones para la carga de logos
	public static final boolean			APP_TEST					= false; //En true desbloquea funciones de alpha para simular push, sms, enviar DB, entre otras
	public static final boolean			DEBUG						= true; //En true desbloquea la impresión de trackers y variables para debug
	public static final boolean			DEBUGDB						= false; //En true desbloquea la impresión de consultas en la db para debug
	public static final double			DENSITY_HDPI				= 1.5; //Indica que se trata de un dispositivo con una resolución menor a HD
	public static final double			DENSITY_XHDPI				= 2.0; //Indica que se trata de un dispositivo con una resolución HD
	public static final double			DENSITY_XXHDPI				= 3.0; //Indica que se trata de un dispositivo con una resolución FullHD
	public static final float			ALPHA_FOR_BLOCKS			= 0.4f; //Indica que nivel de alpha será aplicado sobre el color de los títulos cuando la company está bloqueada
	public static final float			ALPHA_FOR_SUBTITLE			= 0.8f; //Indica que nivel de alpha será aplicado sobre el color de los subtítulos en la toolbar
	public static final int				ANOTHER_SCREEN				= 3; //Indica que se trata de una pantalla común que no requiere tratamiento especial al analizar la sessión
	public static final int				API_LEVEL					= android.os.Build.VERSION.SDK_INT; //Indica la api en la que se está ejecutando la app
	public static final int				BOOL_NO						= 0; //Equivalente false de boolean en int para funciones especiales
	public static final int				BOOL_YES					= 1; //Equivalente true de boolean en int para funciones especiales
	public static final int				CODE_SCREEN					= 2; //Indica que se trata de la pantalla de verificación de código
	public static final int				CODE_LENGTH					= 4; //Longitud del código de verificación
	public static final int				DAYS_UNTIL_PROMPT			= 3; //Días necesarios para lanzar dialog sugiriendo que se califique la app en Play Store
	public static final int				PHONE_SCREEN				= 1; //Indica que se trata de la pantalla de ingreso en la que se pide el celular y país
	public static final int				REALMDB_VERSION				= 15; //Indica el número de versión para la base de datos Realm que está usuando la app
	public static final int				SPLASH_SCREEN				= 0; //Indica que se trata de la pantalla incial en la que arranca la app
	public static final String			CODE_FORMAT					= "+00"; //Formato placeholder para el código de País
	public static final String			COLOR_ACCENT				= "#00BCD4"; //Color de acento en String
	public static final String			COLOR_ACTION				= "#FF8F00"; //Color primario en String
	public static final String			COLOR_BLOCKED				= "#212121"; //Color de company bloqueada en String
	public static final String			COLOR_COMMENT				= "#007AF6"; //Color de para textos de pantalla comentarios en String
	public static final String			COLOR_GRAY					= "#717171"; //Color de filtro para iconos en String
	public static final String			GCM_DEFAULTSENDERID			= "189459365557"; //Referencia al proyecto en la Google Developers Console
	public static final String			HASH_GOOGLEANALYTICS		= "UA-15307457-3"; //Referencia a la cuenta de Google Analytics
	public static final String			HASH_NEWRELIC				= "AA5368393ae01f29d6d74776d5558f53066a8f187c"; //Referencia a la cuenta de NewRelic
	public static final String			KEY_CODE					= "code";
	public static final String			KEY_CONTENT					= "content";
	public static final String			KEY_DATA					= "data";
	public static final String			KEY_DESCRIPTION				= "description";
	public static final String			KEY_DISPLAYNAME				= "displayName";
	public static final String			KEY_FIRSTTIME				= "firstTime";
	public static final String			KEY_ENRICH					= "enrich";
	public static final String			KEY_GEO						= "geolocalization";
	public static final String			KEY_GEO_LAT					= "latitude";
	public static final String			KEY_GEO_LON					= "longitude";
	public static final String			KEY_GEO_SOURCE				= "source";
	public static final String			KEY_ID						= "id";
	public static final String			KEY_IDMONGO					= "_id";
	public static final String			KEY_INFO					= "info";
	public static final String			KEY_LAST_MSGID				= "lastMessageId";
	public static final String			KEY_NAME					= "name";
	public static final String			KEY_PREF					= "vcpref";
	public static final String			KEY_PREF_CALLME				= "callme"; //Clave de preferencia para activar o desactivar llamadas
	public static final String			KEY_PREF_CALLME_TIMES		= "callmeTimes"; //Clave de preferencia para contar llamadas solicitadas
	public static final String			KEY_PREF_CAPTURED			= "captured"; //Clave de preferencia para indicar que al usuario ya se le procesaron los sms con éxito
	public static final String			KEY_PREF_CHECKED			= "checked"; //Clave de preferencia para indicar que el usuario ya fue verificado con éxito
	public static final String			KEY_PREF_DATE_1STLAUNCH		= "dateFirstLaunch"; //Clave de preferencia que indica el primer día desde la intalación de este update
	public static final String			KEY_PREF_DELAY_RATE			= "delayRate"; //Clave de preferencia que indica la cantidad de veces que el usuario dice "Más Tarde" cuando se sugiere calificar la app
	public static final String			KEY_PREF_FREEPASS			= "freePassOn"; //Clave de preferencia para indicar que el usuario se registró y espera el código de verificación
	public static final String			KEY_PREF_LOGGED				= "logged"; //Clave de preferencia para indicar que el usuario se registró y espera el código de verificación
	public static final String			KEY_PREF_NO_RATE			= "iWontRate"; //Clave de prefencia que indica si el usuario oprimió en "No, gracias" cuando se le sugirió calificar la app
	public static final String			KEY_PREF_SPLASHED			= "splashed"; //Clave de preferencia para indicar que ya se efectuó el splash de la primera vez
	public static final String			KEY_PREF_TSCOMPANIES		= "time2UpdateCompanies"; //Clave de preferencia para guardar la última vez que se actualizaron los datos de las companies
	public static final String			KEY_PREF_TSSUBSCRIPTIONS	= "time2UpdateSubscriptions"; //Clave de preferencia para guardar la última vez que se actualizaron los datos de las suscripciones
	public static final String			KEY_PREF_TSUSER				= "time2UpdateUser"; //Clave de preferencia para guardar la última vez que se actualizaron los datos del usuario
	public static final String			KEY_PREF_UPGRADED			= "upgraded"; //Clave de preferencia que almacena si el usuario cumplió con el proceso procesado necesario luego de un update
	public static final String			KEY_PREF_WELCOME			= "welcome"; //Clave de preferencia que indica si el usuario ya vió el tutorial inicial
	public static final String			KEY_REFRESH					= "refresh"; //Clave para indicar si es necesario refrescar la activity a la que se ingresa
	public static final String			KEY_RESPONSE				= "response";
	public static final String			KEY_SECTION					= "section";
	public static final String			KEY_SEND_STATISTICS			= "sendStatistics";
	public static final String			KEY_SOUND					= "sound";
	public static final String			KEY_STATUS					= "status";
	public static final String			KEY_TITLE					= "title";
	public static final String			KEY_TOKEN					= "token";
	public static final String			KEY_TYPE					= "type";
	public static final String			MAIL_ADDRESSEE				= "david.figueroa@tween.com.ar";
	public static final String			MAIL_TWEEN					= "soporte@vloom.io";
	public static final String			REALMDB_NAME				= "viacelular.realm";
	public static final String			REALMDB_PATH				= "/data/data/com.tween.viacelular/files/";
	public static final String			VALUE_FEEDBACKAPPBOY		= "Feedback reply from vloom";
}
