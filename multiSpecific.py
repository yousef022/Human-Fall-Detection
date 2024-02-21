##############################################################
# Pings any of the specific beacons that is closest in range #
##############################################################

from flask import Flask
import asyncio
from bleak import BleakClient, BleakScanner

app = Flask(__name__)

LED_CONTROL_CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"  

# List of specific beacon addresses
specific_beacons = ["ce:e4:05:be:70:cd", "c3:88:f6:29:a9:de", "ee:af:b9:2e:2c:33", "fd:82:b0:4c:91:bf","d2:8c:33:3d:cc:0c"] # Replace

async def find_closest_beacon():
    devices = await BleakScanner.discover()
    closest_device = None
    max_rssi = -100  # Start with a very low RSSI value

    for device in devices:
        if device.address in specific_beacons and device.rssi > max_rssi:
            max_rssi = device.rssi
            closest_device = device

    return closest_device

@app.route('/')
def trigger_led():
    closest_beacon = asyncio.run(find_closest_beacon())
    if closest_beacon:
        print(f"Closest beacon: {closest_beacon.address}")
        asyncio.run(turn_on_led(closest_beacon.address))
        return "LED Triggered on closest beacon"
    else:
        return "No specified beacon found"

async def turn_on_led(beacon_address):
    async with BleakClient(beacon_address) as client:
        connected = await client.is_connected()
        if connected:
            await client.write_gatt_char(LED_CONTROL_CHARACTERISTIC_UUID, bytearray([0x01]))
            print("LED should now be on.")
        else:
            print("Failed to connect to the beacon.")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)