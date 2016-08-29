package com.tween.viacelular.utils;

import android.annotation.SuppressLint;
import android.util.Patterns;
import com.tween.viacelular.models.Message;

@SuppressLint("DefaultLocale")
public class StringUtils
{
	/**
	 * Verifica si el contenido del campo unsuscribe es correcto
	 * @param method
	 * @return boolean
	 */
	public static boolean isValidUnsuscribe(String[] method)
	{
		if(method.length == 3)
		{
			if(method[0].trim().equals(Message.TYPE_SMS) && StringUtils.isNotEmpty(method[2].trim()))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isValidPhone(String phone)
	{
		//Se quitó la limitación de longitud fija
		String mobilePattern = "[0-9]{" + phone.length() + "}";
		return phone.matches(mobilePattern);
	}

	public static boolean isValidCode(String code)
	{
		String mobilePattern = "[0-9]{" + Common.CODE_LENGTH + "}";
		return code.matches(mobilePattern);
	}

	/**
	 * Limpia el texto del mensaje para evitar problemas en la api
	 * @param text
	 * @return String
	 */
	public static String sanitizeText(String text)
	{
		//Se admiten : , . /
		text	= text.replace("\\n", "").replace("\n", "").replace("\\r", "").replace("\r", "").replace("\\t", "").replace("\t", "").replace(";", "");
		text	= text.replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace("*", "").replace("\"", "").replace("'", "").replace("`", "");
		text	= text.replace("\\", "");
		return text;
	}

	public static boolean isCompanyNumber(String number)
	{
		number = number.replace("+", "");

		if(isPhoneNumber(number))
		{
			boolean isNumber = isNumber(number);

			if(isNumber && number.length() <= 7)
			{
				return true;
			}
			else
			{
				boolean isLong = isLong(number);
				return (!isNumber && number.length() < 12 && !isLong);
			}
		}
		else
		{
			return false;
		}
	}

	public static boolean isPhoneNumber(String number)
	{
		number = number.replace("+", "");
		return !number.equals("Restringido") && !number.equals("Privado");
	}

	/**
	 * Verifica si el texto recibido es alfanúmerico
	 * @param str
	 * @return boolean
	 */
	public static boolean isAlphanumeric(String str)
	{
		if(isNotEmpty(str))
		{
			for(int i=0; i<str.length(); i++)
			{
				char c = str.charAt(i);

				if(!Character.isDigit(c) && !Character.isLetter(c))
				{
					return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Verifica si el companyId es de mongoDB o de la db interna en la app
	 * @param companyId
	 * @return boolean result
	 */
	public static boolean isIdMongo(String companyId)
	{
		boolean result = false;

		if(isNotEmpty(companyId))
		{
			if(companyId.length() >= 24 && isAlphanumeric(companyId))
			{
				result = true;
			}
		}

		return result;
	}

	public static boolean isLong(String number)
	{
		try
		{
			if(isNotEmpty(number))
			{
				long num = Long.parseLong(number);
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}

	public static boolean isNumber(String number)
	{
		try
		{
			if(isNotEmpty(number))
			{
				int num = Integer.parseInt(number);
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}

	public static String getInitials(String text)
	{
		if(isValidEmail(text))
		{
			String[] email	= text.split("@");
			text			= email[0];
		}

		String result			= text;
		text					= text.replace(".", " ");
		text					= text.replace("-", " ");
		text					= text.replace("_", " ");
		String[] textSplited	= text.split(" ");

		if(textSplited.length > 1)
		{
			if(StringUtils.isNotEmpty(textSplited[1]))
			{
				result = String.valueOf(textSplited[0].trim().charAt(0)).toUpperCase() + String.valueOf(textSplited[1].trim().charAt(0)).toUpperCase();
			}
			else
			{
				result = String.valueOf(textSplited[0].trim().charAt(0)).toUpperCase();
			}
		}
		else
		{
			if(text.length() >= 6)
			{
				for(int i = 1; i < text.length(); i++)
				{
					if(StringUtils.isNotEmpty(String.valueOf(text.charAt(i))))
					{
						if(String.valueOf(text.charAt(i)).equals(String.valueOf(text.charAt(i)).toUpperCase()))
						{
							result = String.valueOf(textSplited[0].trim().charAt(0)).toUpperCase() + String.valueOf(text.charAt(i)).toUpperCase();
						}
					}
				}
			}
			else
			{
				if(isEmpty(text))
				{
					result = "VC";
				}
				else
				{
					result = String.valueOf(text.trim().charAt(0)).toUpperCase();
				}
			}
		}

		if(result.length() > 2)
		{
			result = result.substring(0, 2);
		}

		return result;
	}

	/**
	 * Verifica si el String está contenido dentro de las keywords
	 * @param text
	 * @param keywords
	 * @return
	 */
	public static boolean containsKeywords(String text, String keywords)
	{
		//Modificaciones por validaciones extra
		if(isNotEmpty(text) && isNotEmpty(keywords))
		{
			keywords = fixListFields(keywords);

			if(keywords.contains(","))
			{
				String[] parts = keywords.split(",");

				for(String part : parts)
				{
					if(isNotEmpty(part))
					{
						if(text.toUpperCase().contains(part.toUpperCase()))
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}
	
	public static Boolean isEmpty(String text)
	{
		return !(text != null && text.trim().length() > 0 && !text.trim().toLowerCase().equals("null") && !text.trim().equals(""));
	}
	
	public static Boolean isNotEmpty(String text)
	{
		return !isEmpty(text);
	}
	
	public static String capitalize(String text)
	{
		if(isEmpty(text) || (text.length() == 1))
		{
			return text.toUpperCase();
		}
		
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}

	public static String removeSpacesJSON(String result)
	{
		result = result.trim();
		result = result.replace("     ", "");
		result = result.replace("   ", "");
		result = result.replace("  ", " ");
		result = result.replace(" }", "}");
		result = result.replace("{ ", "{");
		result = result.replace(" ]", "]");
		result = result.replace("[ ", "[");
		result = result.replace("[,", "[");
		return result;
	}

	public static String fixListFields(String list)
	{
		//Modificaciones por validaciones extra
		if(isNotEmpty(list))
		{
			list = list.trim();
			list = list.replace(",,", ",");
			list = list.replace(" ,", ",");
			list = list.replace(", ", ",");

			if(list.charAt(0) == ',')
			{
				list = list.substring(1, list.length());
			}
		}
		else
		{
			list = "";
		}

		return list;
	}
	
	/**
	 * @param target
	 * @return
	 */
	private static boolean isValidEmail(String target)
	{
		return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}

	public static String capitaliseString(String text)
	{
		if(isNotEmpty(text))
		{
			text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
		}

		return text;
	}
}
