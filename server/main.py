##########################################################################################
# Main Flask server for communication between the beacon and device that detected a fall #
##########################################################################################

from flask import Flask
import asyncio
from bleak import BleakClient

app = Flask(__name__)

BEACON_ADDRESS = "CE:E4:05:BE:70:CD" # Replace with your beacon's MAC address
LED_CONTROL_CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"  # Replace with the actual UUID


@app.route('/')
def trigger_led():
    asyncio.run(turn_on_led())
    return "LED Triggered" 

async def turn_on_led():
    async with BleakClient(BEACON_ADDRESS) as client:
        connected = await client.is_connected()
        if connected:
            # The value to write depends on the beacon's specification
            await client.write_gatt_char(LED_CONTROL_CHARACTERISTIC_UUID, bytearray([0x01]))
            print("LED should now be on.")
    print("LED On")

if __name__ == 'main':
    app.run(host='0.0.0.0', port=8080)