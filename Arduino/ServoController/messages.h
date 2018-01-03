#define BUFFER_SIZE 10
#define MAX_MSG_SIZE 20

class Messages {
  private:
    boolean msgAvailable = false;  // whether the msg is complete
    int bufferPointer = 0;
    int msgCount = 0;
    int lastRead = 0;
    String msgs[BUFFER_SIZE];

  public:    
    Messages();
    void anySerialEvent();
    String read(boolean blocking);
};

