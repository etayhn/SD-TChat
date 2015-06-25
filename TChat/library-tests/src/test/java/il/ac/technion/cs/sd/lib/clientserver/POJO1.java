package il.ac.technion.cs.sd.lib.clientserver;


public class POJO1
{
	public int i;
	public String str;
	POJO1(int i, String str) {
		this.i = i;
		this.str = str;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		POJO1 other = (POJO1) obj;
		if (i != other.i)
			return false;
		if (str == null) {
			if (other.str != null)
				return false;
		} else if (!str.equals(other.str))
			return false;
		return true;
	}
}

