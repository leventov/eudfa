from python.utils import prefix

class Ring:
	def __init__(self, first, *accepts):
		try:
			self.states = list(first)
			self.mod = len(first)
		except TypeError:
			self.mod = first
			accepts = set(a % first for a in accepts)
			self.states = [i in accepts for i in range(first)]


	_accepts = None
	def get_accepts(self):
		if not self._accepts:
			self._accepts = [i for i in range(self.mod) if self.states[i]]
		return self._accepts
	accepts = property(get_accepts)

	def __str__(self):
		return '{{{0}}} mod {1}'.format(', '.join(str(i) for i in self.accepts), self.mod)


	def accepting(self, len):
		return self.states[len % self.mod]

	def simplify(self):
		p = prefix(self.states * 2)
		for new_mod in range(1, self.mod):
			if all(p[i] >= new_mod for i in range(2*new_mod-1, self.mod+new_mod, new_mod)):
				self.mod = new_mod
				self.states = self.states[:new_mod]
				return


class BruteForceRing(Ring):
	def __mul__(self, other):
		a, b = self.mod, other.mod
		while b:
			a, b = b, a % b
		mod = self.mod * other.mod / a
		starts = []
		for base in range(0, mod, self.mod):
			for ac in self.accepts:
				starts.append(base + ac)

		states = []
		for i in range(mod):
			states.append(self.accepting(i) or any(other.accepting(i-s) for s in starts))
		result = Ring(states)
		result.simplify()
		return result




