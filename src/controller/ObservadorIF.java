package controller;

/** Observador: é avisado pelo observado quando o estado da partida muda. */
public interface ObservadorIF {
	void notify(ObservadoIF o);
}
