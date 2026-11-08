package controller;

/** Observado: mantém os observadores registrados e informa os dados da mudança via get(int). */
public interface ObservadoIF {
	void add(ObservadorIF o);

	void remove(ObservadorIF o);

	int get(int i);
}
