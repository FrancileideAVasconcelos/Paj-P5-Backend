package pt.uc.dei.proj5.utils;

public enum LeadState {
    NOVO(1),
    ANALISE(2),
    PROPOSTA(3),
    GANHO(4),
    PERDIDO(5);

    private final int leadStateId;

    LeadState(int leadStateId) {
        this.leadStateId = leadStateId;
    }

    public int getStateId() {
        return leadStateId;
    }

    // Método utilitário para converter ID do Banco/API de volta para Enum
    public static LeadState fromId(int id) {
        for (LeadState state : values()) {
            if (state.leadStateId == id) return state;
        }
        throw new IllegalArgumentException("ID de estado inválido: " + id);
    }
}