package wavebl.pbs.repository.key;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//can be list of available public keys for client but make only one available
public class ClientPublicKeyStorage {
    private final ConcurrentMap<Integer, String> storage;

    public ClientPublicKeyStorage() {
        storage = new ConcurrentHashMap<>();
    }

    public boolean addKey(Integer clientId, String publicKey) {
        String result = storage.computeIfAbsent(clientId, v -> publicKey);
        return result.equals(publicKey);
    }

    public boolean isClientKey(Integer clientId, String publicKey) {
        return publicKey.equals(storage.get(clientId));
    }

}
