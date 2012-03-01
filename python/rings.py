# -*- coding: utf-8 -*-
from utils import prefix, lcm

'''
	Итог: на питоне возможны только совсем песочные вычисления
	Пробуем Яву
'''


class Ring:
	def __init__(self, first, *accepts):
		try: # first как список состояний автомата
			self.states = list(first)
			self.mod = len(first)
		except TypeError: # first как длина кольца (модуль)
			self.mod = first
			accepts = set(a % first for a in accepts)
			self.states = [i in accepts for i in range(first)]

	def __eq__(self, other):
		return self.mod == other.mod and self.states == other.states

	def __ne__(self, other):
		return self.mod != other.mod or self.states != other.states

	def __mul__(self, other):
		mod = lcm(self.mod, other.mod)
		starts = []
		for base in range(0, mod, self.mod):
			for ac in self.accepts:
				starts.append(base + ac)

		states = []
		for i in range(mod):
			states.append(self.accepting(i) or
						  any(other.accepting(i-s) for s in starts))
		return Ring(states).simplified()

	_accepts = None
	def get_accepts(self):
		if not self._accepts:
			self._accepts = [i for i in range(self.mod) if self.states[i]]
		return self._accepts
	accepts = property(get_accepts)

	def __str__(self):
		return '{1}{{{0}}}'.format(', '.join(str(i) for i in self.accepts),
								   self.mod)


	def accepting(self, len):
		return self.states[len % self.mod]

	def simplified(self):
		p = prefix(self.states * 2)
		for new_mod in range(1, self.mod):
			if all(p[i] >= new_mod
				   for i in range(2*new_mod-1, self.mod+new_mod, new_mod)):
				return Ring(self.states[:new_mod])
		return self

	def simple(self):
		return self == self.simplified()

simplify = lambda r: r.simplified()

ONE = Ring(1, 0)



def gen_rings(len):
	rings = []
	for i in range(2**len):
		rings.append(Ring([bool((i>>j)&1) for j in range(len)]))
	return rings

def all_products(*lens):
	products = []
	all_rings = []
	for len in lens:
		len_rings = filter(lambda r: r.simple(), gen_rings(len))
		for r in len_rings:
			for small in all_rings:
				products.append((r, small, r*small))
		all_rings += len_rings
	return products

def print_eq(e):
	print '{0!s} * {1!s} = {2!s}'.format(*e)






