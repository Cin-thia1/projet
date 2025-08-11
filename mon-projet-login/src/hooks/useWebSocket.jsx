import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

const useWebSocket = (url, topic, callback) => {
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: url,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      client.subscribe(topic, (message) => {
        callback(JSON.parse(message.body));
      });
    };

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
    };
  }, [url, topic, callback]);

  return stompClient;
};

export default useWebSocket;