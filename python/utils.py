
def prefix(s):
	z = [0]
	k = 0
	for i in range(1, len(s)):
		while k > 0 and s[i] != s[k]:
			k = z[k-1]
		if s[k] == s[i]:
			k += 1
		z.append(k)
	return z
