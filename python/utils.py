# -*- coding: utf-8 -*-
def prefix(s):
	p = [0]
	k = 0
	for i in range(1, len(s)):
		while k > 0 and s[i] != s[k]:
			k = p[k-1]
		if s[k] == s[i]:
			k += 1
		p.append(k)
	return p

def lcm(a, b):
	p = a * b
	while b:
		a, b = b, a % b
	return p / a
