package utilities

class N2T {

	var flag : Int = 0
	var numero: Int = 0
	var importe_parcial: String = ""
	var num: String = ""
	var num_letra: String = ""
	var num_letras: String = ""
	var num_letram: String = ""
	var num_letradm: String = ""
	var num_letracm: String = ""
	var num_letramm: String = ""
	var num_letradmm: String = ""
	
	  private def unidad(numero: Int): String = {
			var num: String = ""
			numero match {
			case 9 =>
				num = "nueve"
			case 8 =>
				num = "ocho"
			case 7 =>
				num = "siete"
			case 6 =>
				num = "seis"
			case 5 =>
				num = "cinco"
			case 4 =>
				num = "cuatro"
			case 3 =>
				num = "tres"
			case 2 =>
				num = "dos"
			case 1 =>
				if (flag == 0)
					num = "uno"
				else 
					num = "un"
			case 0 =>
				num = ""
			}
			num
	}
	
	private def decena(numero: Int): String = {
			if (numero >= 90 && numero <= 99)
		{
			num_letra = "noventa "
			if (numero > 90)
				num_letra = num_letra + "y " + (unidad(numero - 90))
		}
		else if (numero >= 80 && numero <= 89)
		{
			num_letra = "ochenta "
			if (numero > 80)
				num_letra = num_letra + "y " + unidad(numero - 80)
		}
		else if (numero >= 70 && numero <= 79)
		{
			num_letra = "setenta "
			if (numero > 70)
				num_letra = num_letra + "y " + unidad(numero - 70)
		}
		else if (numero >= 60 && numero <= 69)
		{
			num_letra = "sesenta "
			if (numero > 60)
				num_letra = num_letra + "y " + unidad(numero - 60)
		}
		else if (numero >= 50 && numero <= 59)
		{
			num_letra = "cincuenta "
			if (numero > 50)
				num_letra = num_letra + "y " + unidad(numero - 50)
		}
		else if (numero >= 40 && numero <= 49)
		{
			num_letra = "cuarenta "
			if (numero > 40)
				num_letra = num_letra + "y " + unidad(numero - 40)
		}
		else if (numero >= 30 && numero <= 39)
		{
			num_letra = "treinta "
			if (numero > 30)
				num_letra = num_letra + "y " + unidad(numero - 30)
		}
		else if (numero >= 20 && numero <= 29)
		{
			if (numero == 20)
				num_letra = "veinte "
			else
				num_letra = "veinti" + unidad(numero - 20)
		}
		else if (numero >= 10 && numero <= 19)
		{
			numero match {
			case 10 =>
				num_letra = "diez "
			case 11 =>
				num_letra = "once "
			case 12 =>
				num_letra = "doce "
			case 13 =>
				num_letra = "trece "
			case 14 =>
				num_letra = "catorce "
			case 15 =>
				num_letra = "quince "
			case 16 =>
				num_letra = "dieciseis "
			case 17 =>
				num_letra = "diecisiete "
			case 18 =>
				num_letra = "dieciocho "
			case 19 =>
				num_letra = "diecinueve "
			}	
		}
		else
			num_letra = unidad(numero)
	return num_letra
	}	

	private def centena(numero: Int): String = {
		if (numero >= 100)
		{
			if (numero >= 900 && numero <= 999)
			{
				num_letra = "novecientos "
				if (numero > 900)
					num_letra = num_letra + decena(numero - 900)
			}
			else if (numero >= 800 && numero <= 899)
			{
				num_letra = "ochocientos "
				if (numero > 800)
					num_letra = num_letra + decena(numero - 800)
			}
			else if (numero >= 700 && numero <= 799)
			{
				num_letra = "setecientos "
				if (numero > 700)
					num_letra = num_letra + decena(numero - 700)
			}
			else if (numero >= 600 && numero <= 699)
			{
				num_letra = "seiscientos "
				if (numero > 600)
					num_letra = num_letra + decena(numero - 600)
			}
			else if (numero >= 500 && numero <= 599)
			{
				num_letra = "quinientos "
				if (numero > 500)
					num_letra = num_letra + decena(numero - 500)
			}
			else if (numero >= 400 && numero <= 499)
			{
				num_letra = "cuatrocientos "
				if (numero > 400)
					num_letra = num_letra + decena(numero - 400)
			}
			else if (numero >= 300 && numero <= 399)
			{
				num_letra = "trescientos "
				if (numero > 300)
					num_letra = num_letra + decena(numero - 300)
			}
			else if (numero >= 200 && numero <= 299)
			{
				num_letra = "doscientos "
				if (numero > 200)
					num_letra = num_letra + decena(numero - 200)
			}
			else if (numero >= 100 && numero <= 199)
			{
				if (numero == 100)
					num_letra = "cien "
				else
					num_letra = "ciento " + decena(numero - 100)
			}
		}
		else
			num_letra = decena(numero)
		
		return num_letra;	
	}	

	private def miles(numero: Int): String = {
		if (numero >= 1000 && numero <2000){
			num_letram = ("mil " + centena(numero%1000))
		}
		if (numero >= 2000 && numero <10000){
			flag=1;
			num_letram = unidad(numero/1000) + " mil " + centena(numero%1000)
		}
		if (numero < 1000)
			num_letram = centena(numero)
		
		return num_letram
	}		

	private def decmiles(numero: Int): String = {
		if (numero == 10000)
			num_letradm = "diez mil";
		if (numero > 10000 && numero <20000){
			flag=1;
			num_letradm = decena(numero/1000) + "mil " + centena(numero%1000)
		}
		if (numero >= 20000 && numero <100000){
			flag=1;
			num_letradm = decena(numero/1000) + " mil " + miles(numero%1000)	
		}
		
		
		if (numero < 10000)
			num_letradm = miles(numero)
		
		return num_letradm
	}		

	private def cienmiles(numero: Int): String = {
		if (numero == 100000)
			num_letracm = "cien mil"
		if (numero >= 100000 && numero <1000000){
			flag=1;
			num_letracm = centena(numero/1000) + " mil " + centena(numero%1000)
		}
		if (numero < 100000)
			num_letracm = decmiles(numero)
		return num_letracm
	}		

	private def millon(numero: Int): String = {
		if (numero >= 1000000 && numero <2000000){
			flag=1
			num_letramm = ("Un millon ") + cienmiles(numero%1000000)
		}
		if (numero >= 2000000 && numero <10000000){
			flag=1
			num_letramm = unidad(numero/1000000) + " millones " + cienmiles(numero%1000000)
		}
		if (numero < 1000000)
			num_letramm = cienmiles(numero)
		
		return num_letramm
	}		
	
	private def decmillon(numero: Int): String = {
		if (numero == 10000000)
			num_letradmm = "diez millones"
		if (numero > 10000000 && numero <20000000){
			flag=1
			num_letradmm = decena(numero/1000000) + "millones " + cienmiles(numero%1000000)
		}
		if (numero >= 20000000 && numero <100000000){
			flag=1
			num_letradmm = decena(numero/1000000) + " milllones " + millon(numero%1000000)
		}
		
		
		if (numero < 10000000)
			num_letradmm = millon(numero)
		
		return num_letradmm
	}

	  def convertirLetras(numero: Int): String = {
		num_letras = decmillon(numero)
		return num_letras
	} 	
}

object N2T extends N2T